package chat.sphinx.features.connect_manager

import chat.sphinx.concepts.connect_manager.ConnectManager
import chat.sphinx.concepts.connect_manager.ConnectManagerListener
import chat.sphinx.concepts.connect_manager.model.OwnerInfo
import chat.sphinx.concepts.connect_manager.model.RestoreProgress
import chat.sphinx.concepts.connect_manager.model.RestoreState
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.wrapper.contact.NewContact
import chat.sphinx.wrapper.lightning.WalletMnemonic
import chat.sphinx.wrapper.lightning.toLightningNodePubKey
import chat.sphinx.wrapper.lightning.toLightningRouteHint
import chat.sphinx.wrapper.mqtt.ConnectManagerError
import chat.sphinx.wrapper.mqtt.MsgsCounts
import chat.sphinx.wrapper.mqtt.NewInvite
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackDynamicSerializer
import io.ktor.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import uniffi.sphinxrs.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.collections.LinkedHashSet
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.text.toCharArray

class ConnectManagerImpl(
    private val LOG: SphinxLogger
) : ConnectManager() {

    private var _mixerIp: String? = null
    private var walletMnemonic: WalletMnemonic? = null
    private var mqttClient: MqttAsyncClient? = null
    private var network = ""
    private var ownerSeed: String? = null
    private var inviteCode: String? = null
    private var inviteInitialTribe: String? = null
    private var currentInvite: NewInvite? = null
    private var restoreMnemonicWords: List<String>? = emptyList()
    private var inviterContact: NewContact? = null
    private var hasAttemptedReconnect = false
    private var tribeServer: String? = null
    private var serverDefaultTribe: String? = null
    private var router: String? = null
    private val pingsMap = mutableMapOf<String, Long>()
    private var readyForPing: Boolean = false
    private var delayedRRObjects: MutableList<RunReturn> = mutableListOf()
    private val restoreProgress = RestoreProgress()
    private var isMqttConnected: Boolean = false
    private var isAppFirstInit: Boolean = true
    private var ownerUserName: String? = null

    companion object {
        const val TEST_V2_SERVER_IP = "75.101.247.127:1883"
        const val TEST_V2_TRIBES_SERVER = "75.101.247.127:8801"
        const val REGTEST_NETWORK = "regtest"
        const val MAINNET_NETWORK = "bitcoin"
        const val TEST_SERVER_PORT = 1883
        const val PROD_SERVER_PORT = 8883
        const val COMPLETE_STATUS = "COMPLETE"
        const val MSG_BATCH_LIMIT = 100
        const val MSG_FIRST_PER_KEY_LIMIT = 100

        // MESSAGES TYPES
        const val TYPE_CONTACT_KEY = 10
        const val TYPE_CONTACT_KEY_CONFIRMATION = 11
        const val TYPE_GROUP_JOIN = 14
        const val TYPE_MEMBER_APPROVE = 20
        const val TYPE_CONTACT_KEY_RECORD = 33
    }

    private val _ownerInfoStateFlow: MutableStateFlow<OwnerInfo> by lazy {
        MutableStateFlow(
            OwnerInfo(
                null,
                null,
                null,
                null
            )
        )
    }
    override val ownerInfoStateFlow: StateFlow<OwnerInfo>
        get() = _ownerInfoStateFlow.asStateFlow()

    private val _restoreStateFlow: MutableStateFlow<RestoreState?> by lazy {
        MutableStateFlow(null)
    }
    override val restoreStateFlow: StateFlow<RestoreState?>
        get() = _restoreStateFlow.asStateFlow()

    override val msgsCountsState: MutableStateFlow<MsgsCounts?> by lazy {
        MutableStateFlow(null)
    }

    private var mixerIp: String?
        get() = _mixerIp?.let {
            if (isProductionEnvironment()) {
                if (!it.startsWith("ssl://")) "ssl://$it" else it
            } else {
                if (!it.startsWith("tcp://")) "tcp://$it" else it
            }
        }
        set(value) {
            _mixerIp = value?.replace("tcp://", "")?.replace("ssl://", "")
        }

    private fun getRawMixerIp(): String? {
        return _mixerIp
    }

    private fun connectToMQTT(
        serverURI: String,
        clientId: String,
        key: String,
        password: String,
    ) {
        try {
            mqttClient = MqttAsyncClient(serverURI, clientId, null)

            val sslContext: SSLContext? = if (isProductionEnvironment()) {
                SSLContext.getInstance("TLS").apply {
                    val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }
                    })
                    init(null, trustAllCerts, SecureRandom())
                }
            } else {
                null
            }

            val options = MqttConnectOptions().apply {
                this.userName = key
                this.password = password.toCharArray()
                this.socketFactory = sslContext?.socketFactory
            }

            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    isMqttConnected = true
                    hasAttemptedReconnect = false

                    subscribeOwnerMQTT()

                    notifyListeners {
                        onNetworkStatusChange(true)
                    }
                    LOG.d("MQTT_MESSAGES", "MQTT CONNECTED!")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    isMqttConnected = false

                    // If it's the first time trying to connect, try to reconnect otherwise
                    // prevent infinite loop
                    if (!hasAttemptedReconnect) {
                        hasAttemptedReconnect = true
                        reconnectWithBackOff()
                    }
                    notifyListeners {
                        onConnectManagerError(ConnectManagerError.MqttConnectError(exception?.message))
                    }
                    LOG.d("MQTT_MESSAGES", "Failed to connect to MQTT: ${exception?.message}")
                }
            })

            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    isMqttConnected = false
                    reconnectWithBackOff()

                    notifyListeners {
                        onConnectManagerError(ConnectManagerError.MqttConnectError(cause?.message))
                    }
                    LOG.d("MQTT_MESSAGES", "MQTT DISCONNECTED! $cause ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // Handle incoming messages here
                    if (topic?.contains("/ping") == true) {

                        notifyListeners {
                            listenToOwnerCreation {
                                LOG.d("MQTT_MESSAGES", "OWNER EXIST!")
                                handleMessageArrived(topic, message)
                            }
                        }
                    } else {
                        handleMessageArrived(topic, message)
                    }
                    LOG.d("MQTT_MESSAGES", "toppicArrived: $topic")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Handle message delivery confirmation here
                }
            })
        } catch (e: MqttException) {
            isMqttConnected = false
            notifyListeners {
                onConnectManagerError(ConnectManagerError.MqttConnectError(e.message))
            }

            reconnectWithBackOff()
            LOG.d("MQTT_MESSAGES", "MQTT DISCONNECTED! exception ${e.printStackTrace()}")
        }
    }

    private fun subscribeOwnerMQTT() {
        try {
            mqttClient?.let { client ->
                // Network setup and handling
                val networkSetup = setNetwork(network)
                handleRunReturn(networkSetup)

                // Initial setup and handling
                val setUp = initialSetup(
                    ownerSeed!!,
                    getTimestampInMilliseconds(),
                    getCurrentUserState(),
                    UUID.randomUUID().toString(),
                    inviterContact?.inviteCode
                )
                handleRunReturn(setUp)

                val qos = IntArray(1) { 1 }

                val tribeSubtopic = getTribeManagementTopic(
                    ownerSeed!!,
                    getTimestampInMilliseconds(),
                    getCurrentUserState()
                )
                client.subscribe(arrayOf(tribeSubtopic), qos)

                if (isRestoreAccount()) {
                    getAllMessagesCount()
                } else if (ownerInfoStateFlow.value.messageLastIndex != null) {
                    val msgLastIndex = ownerInfoStateFlow.value.messageLastIndex?.plus(1)
                    fetchMessagesOnAppInit(
                        msgLastIndex ?: 0,
                        false
                    )
                    notifyListeners {
                        onGetNodes()
                    }
                } else {
                    getPings()
                }
            }
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SubscribeOwnerError)
            }
            LOG.d("MQTT_MESSAGES", "${e.message}")
        }
    }

    private fun handleRunReturn(
        rr: RunReturn,
        skipSettleTopic: Boolean = false,
        skipAsyncTopic: Boolean = false,
        topic: String? = null
    ) {
        if (mqttClient != null) {
            // Set updated state into db
            rr.stateMp?.let {
                notifyListeners { onUpdateUserState(it) }
                LOG.d("MQTT_MESSAGES", "=> stateMp $it")
            }

            if (rr.stateToDelete.isNotEmpty()) {
                notifyListeners { onRemoveKeysFromUserState(rr.stateToDelete) }
                    LOG.d("MQTT_MESSAGES", "=> stateToDelete ${rr.stateToDelete}")
                }

            // Set your balance
            rr.newBalance?.let { newBalance ->
                convertMillisatsToSats(newBalance)?.let { balance ->
                    notifyListeners {
                        onNewBalance(balance)
                    }
                }
                LOG.d("MQTT_MESSAGES", "===> BALANCE ${newBalance.toLong()}")
            }

            processMessages(
                rr.msgs,
                topic
            )

            handlePingDone(rr.msgs)

            // Handling new tribe and tribe members
            rr.newTribe?.let { newTribe ->
                notifyListeners {
                    onNewTribeCreated(newTribe)
                }
                LOG.d("MQTT_MESSAGES", "===> newTribe $newTribe")
            }

            rr.tribeMembers?.let { tribeMembers ->
                notifyListeners {
                    onTribeMembersList(tribeMembers)
                }
                LOG.d("MQTT_MESSAGES", "=> tribeMembers $tribeMembers")
            }

            // Handling my contact info
            rr.myContactInfo?.let { myContactInfo ->
                val parts = myContactInfo.split("_", limit = 2)
                val okKey = parts.getOrNull(0)
                val routeHint = parts.getOrNull(1)

                if (okKey != null && routeHint != null) {
                    notifyListeners {
                        onOwnerRegistered(
                            okKey,
                            routeHint,
                            isRestoreAccount(),
                            getRawMixerIp(),
                            tribeServer,
                            isProductionEnvironment(),
                            router,
                            serverDefaultTribe,
                            ownerUserName
                        )
                    }
                }

                LOG.d("MQTT_MESSAGES", "=> my_contact_info $myContactInfo")
                LOG.d("MQTT_MESSAGES", "=> my_contact_info mixerIp $mixerIp")
                LOG.d("MQTT_MESSAGES", "=> my_contact_info tribeServer $tribeServer")
            }

            // Handling new invite created
            rr.newInvite?.let { invite ->
                LOG.d("MQTT_MESSAGES", "=> new_invite $invite")
            }

            rr.inviterContactInfo?.let { inviterInfo ->
                val parts = inviterInfo.split("_")
                val okKey = parts.getOrNull(0)?.toLightningNodePubKey()
                val routeHint = "${parts.getOrNull(1)}_${parts.getOrNull(2)}".toLightningRouteHint()

                val code = codeFromInvite(inviteCode!!)

                inviterContact = NewContact(
                    null,
                    okKey,
                    routeHint,
                    null,
                    false,
                    null,
                    code,
                    null,
                    null,
                    null
                )

                subscribeOwnerMQTT()

                LOG.d("MQTT_MESSAGES", "=> inviterInfo $inviterInfo")
            }

            rr.msgsCounts?.let { msgsCounts ->
                processMessagesCounts(msgsCounts)
            }

            rr.msgsTotal?.let { msgsTotal ->
                LOG.d("MQTT_MESSAGES", "=> msgsTotal $msgsTotal")
            }

            rr.lastRead?.let { lastRead ->
                notifyListeners {
                    onLastReadMessages(lastRead)
                }
                LOG.d("MQTT_MESSAGES", "=> lastRead $lastRead")
            }

            rr.initialTribe?.let { initialTribe ->
                // Call joinTribe with the url that comes on initialTribe
                inviteInitialTribe = initialTribe
                LOG.d("MQTT_MESSAGES", "=> initialTribe $initialTribe")
            }

            // Handling other properties like sentStatus, settledStatus, error, etc.
            rr.error?.let { error ->
                handleError(error)
            }

            // Sent
            rr.sentStatus?.let { sentStatus ->
                val tagAndStatus = extractTagAndStatus(sentStatus)

                if (tagAndStatus?.first == currentInvite?.tag) {
                    if (tagAndStatus?.second == true) {

                        notifyListeners {
                            onNewInviteCreated(
                                currentInvite?.nickname.orEmpty(),
                                currentInvite?.inviteString ?: "",
                                currentInvite?.inviteCode ?: "",
                                currentInvite?.invitePrice ?: 0L,
                            )
                        }
                        currentInvite = null
                    }
                } else {
                    notifyListeners {
                        onSentStatus(sentStatus)
                    }
                }

                LOG.d("MQTT_MESSAGES", "=> sent_status $sentStatus")
            }

            // Settled
            rr.settledStatus?.let { settledStatus ->
                handleSettledStatus(settledStatus)
                LOG.d("MQTT_MESSAGES", "=> settled_status $settledStatus")
            }

            rr.asyncpayTag?.let { asyncTag ->
                handleAsyncTag(asyncTag)
                LOG.d("MQTT_MESSAGES", "=> asyncpayTag $asyncTag")
            }

            rr.lspHost?.let { lspHost ->
                mixerIp = lspHost
            }

            rr.muteLevels?.let { muteLevels ->
                notifyListeners {
                    onUpdateMutes(muteLevels)
                }
                LOG.d("MQTT_MESSAGES", "=> muteLevels $muteLevels")
            }
            rr.ping?.let { ping ->
                if (ping.isNotEmpty()) {
                    handlePing(ping)
                    LOG.d("MQTT_MESSAGES", "=> ping $ping")
                }
            }
            rr.payments?.let { payments ->
                notifyListeners {
                    onPayments(payments)
                }
                logLongString("PAYMENTS_MESSAGES", payments)
            }
            rr.paymentsTotal?.let { paymentsTotal ->
                LOG.d("MQTT_MESSAGES", "=> paymentsTotal $paymentsTotal")
            }
            rr.tags?.let { tags ->
                notifyListeners {
                    onMessageTagList(tags)
                }
                LOG.d("MQTT_MESSAGES", "=> tags $tags")
            }

            rr.subscriptionTopics.forEach { topic ->
                val qos = IntArray(1) { 1 }
                mqttClient?.subscribe(arrayOf(topic), qos)
                LOG.d("MQTT_MESSAGES", "=> subscribed to $topic")
            }

            if (!skipSettleTopic) {
                rr.settleTopic?.let { settleTopic ->
                    rr.settlePayload?.let { payload ->
                        mqttClient?.publish(settleTopic, MqttMessage(payload))
                        delayedRRObjects.add(rr)
                        LOG.d("MQTT_MESSAGES", "=> settleRunReturn $settleTopic")
                        return
                    }
                }
            }

            handleRegisterTopic(rr, skipAsyncTopic) { runReturn, callbackSkipAsyncTopic ->
                if (!callbackSkipAsyncTopic) {
                    rr.asyncpayTopic?.let { asyncPayTopic ->
                        rr.asyncpayPayload?.let { asyncPayPayload ->
                            mqttClient?.publish(asyncPayTopic, MqttMessage(asyncPayPayload))

                            delayedRRObjects.add(runReturn)

                            LOG.d("MQTT_MESSAGES", "=> asyncpayTopic add $runReturn")
                            return@handleRegisterTopic
                        }
                    }
                }

                rr.topics.forEachIndexed { index, topic ->
                    val payload = rr.payloads.getOrElse(index) { ByteArray(0) }
                    mqttClient?.publish(topic, MqttMessage(payload))
                    LOG.d("MQTT_MESSAGES", "=> published to $topic")
                }
            }
        } else {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.MqttClientError)
            }
        }
    }

    private fun processMessagesCounts(msgsCounts: String) {
        notifyListeners {
            onMessagesCounts(msgsCounts)
        }
        LOG.d("MQTT_MESSAGES", "=> msgsCounts $msgsCounts")
    }

    override fun saveMessagesCounts(msgsCounts: MsgsCounts) {
        msgsCountsState.value = msgsCounts
    }

    private fun processMessages(
        msgs: List<Msg>,
        topic: String?
    ) {
        if (msgs.isNotEmpty()) {
            val tribesToUpdate = msgs.filter {
                it.type?.toInt() == TYPE_MEMBER_APPROVE || it.type?.toInt() == TYPE_GROUP_JOIN
            }.map {
                Pair(it.sender, it.fromMe)
            }

            LOG.d("RESTORE_PROCESS_TRIBE", "$tribesToUpdate")

            notifyListeners {
                onUpsertTribes(tribesToUpdate, isProductionEnvironment()) {
                    val contactsToRestore: List<Pair<String?, Long?>> = msgs.filter {
                        it.type?.toInt() == TYPE_CONTACT_KEY_RECORD ||
                                it.type?.toInt() == TYPE_CONTACT_KEY_CONFIRMATION ||
                                it.type?.toInt() == TYPE_CONTACT_KEY
                    }.map {
                        Pair(it.sender, it.timestamp?.toLong())
                    }

                    LOG.d("RESTORE_PROCESS_CONTACTS", "$contactsToRestore")

                    notifyListeners {
                        onUpsertContacts(contactsToRestore) {
                            // Handle new messages
                            msgs.forEach { msg ->
                                processMessage(msg)
                            }

                            continueRestore(msgs, topic)
                        }
                    }
                }
            }
        } else {
            if (topic?.contains("/batch") == true) {
                goToNextPhaseOrFinish()
            }
        }
    }

    private fun goToNextPhaseOrFinish() {
        if (restoreStateFlow.value is RestoreState.RestoringContacts) {
            notifyListeners { onRestoreProgress(restoreProgress.fixedContactPercentage) }
            notifyListeners { onRestoreMessages() }
        }

        if (restoreStateFlow.value is RestoreState.RestoringMessages || restoreStateFlow.value == null) {
            if (restoreStateFlow.value is RestoreState.RestoringMessages) {
                notifyListeners { onRestoreProgress(restoreProgress.fixedContactPercentage + restoreProgress.fixedMessagesPercentage) }
                _restoreStateFlow.value = RestoreState.RestoreFinished
                notifyListeners { onRestoreFinished() }
            }

            notifyListeners { updatePaidInvoices() }
            getReadMessages()
            getMutedChats()
            getPings()
        }
    }

    private fun continueRestore(
        msgs: List<Msg>,
        topic: String?
    ) {
        if (topic?.contains("/batch") == false) {
            return
        }

        if (isRestoreAccount()) {

            if (restoreStateFlow.value is RestoreState.RestoringContacts) {
                val highestIndex = msgs.maxByOrNull { it.index?.toLong() ?: 0L }?.index?.toLong()
                highestIndex?.let { nnHighestIndex ->
                    calculateContactRestore()

                    msgsCountsState.value?.first_for_each_scid_highest_index?.let { highestIndex ->
                        if (nnHighestIndex < highestIndex) {
                            fetchFirstMessagesPerKey(nnHighestIndex.plus(1L), msgsCountsState.value?.ok_key)
                        } else {
                            goToNextPhaseOrFinish()
                        }
                    }
                }
            } else {
                // Restore Message Step
                if (restoreStateFlow.value is RestoreState.RestoringMessages) {
                    val minIndex = msgs.minByOrNull { it.index?.toLong() ?: 0L }?.index?.toULong()
                    minIndex?.let { nnMinIndex ->
                        calculateMessageRestore()
                        fetchMessagesOnRestoreAccount(nnMinIndex.minus(1u).toLong(), msgsCountsState.value?.total)

                        notifyListeners {
                            onRestoreMinIndex(nnMinIndex.toLong())
                        }
                    }
                }
            }
        } else {
            val highestIndexReceived = msgs.maxByOrNull { it.index?.toLong() ?: 0L }?.index?.toULong()

            highestIndexReceived?.let { nnHighestIndexReceived ->
                fetchMessagesWithPagination(nnHighestIndexReceived)
            }
        }
    }

    private fun processMessage(msg: Msg) {

        notifyListeners {
            onMessage(
                msg.message.orEmpty(),
                msg.sender.orEmpty(),
                msg.type?.toInt() ?: 0,
                msg.uuid.orEmpty(),
                msg.index.orEmpty(),
                msg.timestamp?.toLong(),
                msg.sentTo.orEmpty(),
                msg.msat?.let { convertMillisatsToSats(it) },
                msg.fromMe,
                msg.tag,
                msg.timestamp?.toLong(),
                isRestoreAccount()
            )
        }
    }

    private fun fetchMessagesWithPagination(
        serverHighestIndexRecevied: ULong,
    ) {
        val newHighestIndex = serverHighestIndexRecevied.plus(1u)

        fetchMessagesOnAppInit(
            newHighestIndex.toLong(),
            false
        )
    }

    private fun handleRegisterTopic(
        rr: RunReturn,
        skipAsyncTopic: Boolean,
        callback: (RunReturn, Boolean) -> Unit
    ) {
        if (rr.registerTopic != null && rr.registerPayload != null) {
            val payload = rr.registerPayload!!
            mqttClient?.publish(rr.registerTopic!!, MqttMessage(payload))
            LOG.d("MQTT_MESSAGES", "=> registerTopic ${rr.registerTopic}")

            notifyListeners {
                onPerformDelay(250L) {
                    callback(rr, skipAsyncTopic)
                    LOG.d("MQTT_MESSAGES", "=> delayed performed")
                }
            }
        } else {
            callback(rr, skipAsyncTopic)
        }
    }

    private fun handleAsyncTag(asyncTag: String?) {
        asyncTag?.let { nnAsyncTag ->
            val rrObject = delayedRRObjects.firstOrNull {
                it.msgs.any { msg -> msg.tag == nnAsyncTag }
            }

            rrObject?.let { rr ->
                delayedRRObjects = delayedRRObjects.filter { rr ->
                    rr.msgs.none { msg -> msg.tag == nnAsyncTag }
                }.toMutableList()

                handleRunReturn(
                    rr,
                    skipSettleTopic = true,
                    skipAsyncTopic = true
                )
            }
        }
    }

    // Account Management Methods
    override fun setOwnerInfo(ownerInfo: OwnerInfo) {
        _ownerInfoStateFlow.value = ownerInfo
    }

    override fun updateOwnerInfoUserState(userState: String) {
        _ownerInfoStateFlow.value = ownerInfoStateFlow.value.copy(
            userState = userState
        )
    }

    override fun createAccount(userAlias: String) {
        ownerUserName = userAlias
        if (isRestoreAccount()) {
            notifyListeners {
                onRestoreAccount(isProductionEnvironment())
            }
        } else {
            val seed = generateMnemonicAndSeed(null)
            val now = getTimestampInMilliseconds()

            seed?.let { firstSeed ->
                var invite: ParsedInvite? = null
                ownerSeed = firstSeed

                if (inviteCode != null) {
                    invite = parseInvite(inviteCode!!)

                    val hostAndPubKey = invite.initialTribe?.let { extractTribeServerAndPubkey(it) }
                    val parts = invite.inviterContactInfo?.split("_")
                    val okKey = parts?.getOrNull(0)?.toLightningNodePubKey()
                    val routeHint =
                        "${parts?.getOrNull(1)}_${parts?.getOrNull(2)}".toLightningRouteHint()

                    // add contact alias
                    inviterContact = NewContact(
                        null,
                        okKey,
                        routeHint,
                        null,
                        false,
                        null,
                        invite.code,
                        null,
                        null,
                        null
                    )
                    _mixerIp = invite.lspHost
                    tribeServer = hostAndPubKey?.first
                    inviteInitialTribe = invite.initialTribe

                    network = if (isProductionServer()) {
                        MAINNET_NETWORK
                    } else {
                        REGTEST_NETWORK
                    }
                }

                val xPub = generateXPub(firstSeed, now, network)
                val sig = signMs(firstSeed, now, network)

                if (xPub != null) {
                    connectToMQTT(mixerIp!!, xPub, now, sig)
                } else {
                    notifyListeners {
                        onConnectManagerError(ConnectManagerError.GenerateXPubError)
                    }
                }
            }
        }
    }

    override fun restoreAccount(
        defaultTribe: String?,
        tribeHost: String?,
        mixerServerIp: String?,
        routerUrl: String?
    ) {
        val restoreMnemonic = restoreMnemonicWords?.joinToString(" ")
        val seed = generateMnemonicAndSeed(restoreMnemonic)
        val now = getTimestampInMilliseconds()

        seed?.let { firstSeed ->
            ownerSeed = firstSeed
            _mixerIp = mixerServerIp ?: TEST_V2_SERVER_IP
            tribeServer = tribeHost ?: TEST_V2_TRIBES_SERVER
            router = routerUrl
            serverDefaultTribe = defaultTribe

            network = if (isProductionServer()) {
                MAINNET_NETWORK
            } else {
                REGTEST_NETWORK
            }

            val xPub = generateXPub(firstSeed, now, network)
            val sig = signMs(firstSeed, now, network)

            if (xPub != null) {
                connectToMQTT(mixerIp!!, xPub, now, sig)
            }
        }
    }

    override fun cancelRestore() {
        _restoreStateFlow.value = null
    }

    override fun setInviteCode(inviteString: String) {
        this.inviteCode = inviteString
    }

    override fun setMnemonicWords(words: List<String>?) {
        this.restoreMnemonicWords = words
    }

    override fun setNetworkType(isTestEnvironment: Boolean) {
        if (isTestEnvironment) {
            this.network = REGTEST_NETWORK
        } else {
            this.network = MAINNET_NETWORK
        }
    }

    override fun setOwnerDeviceId(
        deviceId: String,
        pushKey: String
    ) {
        try {
            val token = setPushToken(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                deviceId,
                pushKey
            )
            handleRunReturn(token)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SetDeviceIdError)
            }
            LOG.d("MQTT_MESSAGES", "setOwnerDeviceId ${e.message}")
        }
    }

    override fun processChallengeSignature(challenge: String): String? {
        val signedChallenge = try {
            signBytes(
                ownerSeed!!,
                0.toULong(),
                getTimestampInMilliseconds(),
                network,
                challenge.toByteArray()
            )
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SignBytesError)
            }
            null
        }

        if (signedChallenge != null) {
            val sign = ByteArray(signedChallenge.length / 2) { index ->
                val start = index * 2
                val end = start + 2
                val byteValue = signedChallenge.substring(start, end).toInt(16)
                byteValue.toByte()
            }.encodeBase64()
                .replace("/", "_")
                .replace("+", "-")

            notifyListeners {
                onSignedChallenge(sign)
            }
            return sign
        }
        return null
    }

    override fun fetchFirstMessagesPerKey(lastMsgIdx: Long, totalCount: Long?) {
        try {
            if (lastMsgIdx == 0L) {
                _restoreStateFlow.value = RestoreState.RestoringContacts
                setContactKeyTotal(totalCount)
            }

            val limit = MSG_FIRST_PER_KEY_LIMIT
            val fetchFirstMsg = uniffi.sphinxrs.fetchFirstMsgsPerKey(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                lastMsgIdx.toULong(),
                limit.toUInt(),
                false
            )
            handleRunReturn(fetchFirstMsg)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.FetchFirstMessageError)
            }
            LOG.d("MQTT_MESSAGES", "fetchFirstMessagesPerKey ${e.message}")
        }
    }

    override fun fetchMessagesOnRestoreAccount(
        totalHighestIndex: Long?,
        totalMsgsCount: Long?
    ) {
        try {
            if (restoreStateFlow.value !is RestoreState.RestoringMessages) {
                _restoreStateFlow.value = RestoreState.RestoringMessages
                setMessagesTotal(totalMsgsCount)
            }

            val fetchMessages = fetchMsgsBatch(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                totalHighestIndex?.toULong() ?: 0.toULong(),
                MSG_BATCH_LIMIT.toUInt(),
                true,
            )
            handleRunReturn(fetchMessages)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.FetchMessageError)
            }
            LOG.d("MQTT_MESSAGES", "fetchMessagesOnRestoreAccount ${e.message}")
        }
    }

    override fun getAllMessagesCount() {
        try {
            val messageAmount = getMsgsCounts(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState()
            )
            handleRunReturn(messageAmount)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.MessageCountError)
            }
            LOG.d("MQTT_MESSAGES", "getAllMessagesCount $e")
        }
    }

    override fun initializeMqttAndSubscribe(
        serverUri: String,
        mnemonicWords: WalletMnemonic,
        ownerInfo: OwnerInfo
    ) {
        _mixerIp = serverUri
        walletMnemonic = mnemonicWords
        network = if (isProductionServer()) MAINNET_NETWORK else REGTEST_NETWORK

        if (isConnected()) {
            _ownerInfoStateFlow.value = OwnerInfo(
                ownerInfo.alias,
                ownerInfo.picture,
                ownerInfoStateFlow.value.userState,
                ownerInfo.messageLastIndex
            )

            // It's called when invitee first init the dashboard
            if (inviterContact != null) {
                createContact(inviterContact!!)
                inviterContact = null
            }

            if (inviteInitialTribe != null) {
                notifyListeners {
                    onInitialTribe(inviteInitialTribe!!, isProductionEnvironment())
                }
            }

            getReadMessages()
            getMutedChats()

            notifyListeners {
                onNetworkStatusChange(true)
            }

            return
        }
        _ownerInfoStateFlow.value = ownerInfo

        val seed = try {
            mnemonicToSeed(mnemonicWords.value)
        } catch (e: Exception) {
            null
        }

        val xPub = seed?.let {
            generateXPub(
                it,
                getTimestampInMilliseconds(),
                network
            )
        }

        val now = getTimestampInMilliseconds()

        val sig = seed?.let {
            rootSignMs(
                it,
                now,
                network
            )
        }

        if (xPub != null && sig != null) {
            ownerSeed = seed

            connectToMQTT(
                mixerIp!!,
                xPub,
                now,
                sig,
            )
        } else {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.XPubOrSignError)
            }
        }
    }

    override fun reconnectWithBackOff() {
        notifyListeners {
            onNetworkStatusChange(false, isLoading = true)
        }

        if (!isConnected()) {
            resetMQTT()

            if (mixerIp != null && walletMnemonic != null) {

                initializeMqttAndSubscribe(
                    mixerIp!!,
                    walletMnemonic!!,
                    ownerInfoStateFlow.value,
                )
            }
            LOG.d("MQTT_MESSAGES", "onReconnectMqtt")
        } else {
            getReadMessages()
            getMutedChats()

            notifyListeners {
                onNetworkStatusChange(true)
            }
        }
    }

    override fun disconnectMqtt() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            mqttClient = null
            isMqttConnected = false
            notifyListeners {
                onNetworkStatusChange(false)
            }
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "disconnectMqtt ${e.message}")
        }
    }

    override fun attemptReconnectOnResume() {
        if (isAppFirstInit) {
            isAppFirstInit = false
            return
        } else {
            reconnectWithBackOff()
        }
    }

    override fun retrieveLspIp(): String? {
        return mixerIp
    }

    private fun getPings() {
        try {
            readyForPing = true

            val pings = fetchPings(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState()
            )
            handleRunReturn(pings)
        } catch (e: Exception) {
//            notifyListeners {
//                onConnectManagerError(ConnectManagerError.FetchPingsError)
//            }
            LOG.d("MQTT_MESSAGES", "getPings ${e.message}")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun generateMnemonicAndSeed(restoreMnemonic: String?): String? {
        var seed: String? = null

        // Check if is account restoration
        val mnemonic = if (!restoreMnemonic.isNullOrEmpty()) {
            restoreMnemonicWords!!.joinToString(" ")
        } else {
            try {
                val randomBytes = generateRandomBytes(16)
                val randomBytesString =
                    randomBytes.joinToString("") { it.toString(16).padStart(2, '0') }
                val words = mnemonicFromEntropy(randomBytesString)

                words
            } catch (e: Exception) {
                notifyListeners {
                    onConnectManagerError(ConnectManagerError.GenerateMnemonicError)
                }
                null
            }
        }

        mnemonic?.let { words ->
            try {
                seed = mnemonicToSeed(words)

                notifyListeners {
                    onMnemonicWords(words, isRestoreAccount())
                }

            } catch (e: Exception) {
            }
        }

        return seed
    }

    private fun generateXPub(seed: String, time: String, network: String): String? {
        return try {
            xpubFromSeed(seed, time, network)
        } catch (e: Exception) {
            null
        }
    }

    private fun signMs(seed: String, time: String, network: String): String {
        return try {
            rootSignMs(seed, time, network)
        } catch (e: Exception) {
            ""
        }
    }

    private fun isProductionServer(): Boolean {
        val ip = mixerIp ?: return false
        val port = ip.substringAfterLast(":").toIntOrNull() ?: return false
        return port != TEST_SERVER_PORT
    }

    private fun isProductionEnvironment(): Boolean {
        return network != REGTEST_NETWORK
    }

    private fun processNewInvite(
        seed: String,
        uniqueTime: String,
        state: ByteArray,
        inviteQr: String
    ): RunReturn? {
        return try {
            processInvite(seed, uniqueTime, state, inviteQr)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.ProcessInviteError)
            }
            null
        }
    }

    // Contact Management Methods
    override fun createContact(
        contact: NewContact
    ) {
        val now = getTimestampInMilliseconds()

        println("CREATE_CONTACT: override fun createContact (CONNECT MANG IMPLEMENTATION)")

        try {
            val runReturn = addContact(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                contact.lightningNodePubKey?.value!!,
                contact.lightningRouteHint?.value!!,
                ownerInfoStateFlow.value.alias ?: "",
                ownerInfoStateFlow.value.picture ?: "",
                5000.toULong(),
                contact.inviteCode,
                contact.contactAlias?.value
            )

            handleRunReturn(
                runReturn
            )
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "add contact excp $e")
        }
    }

    override fun createInvite(
        nickname: String,
        welcomeMessage: String,
        sats: Long,
        serverDefaultTribe: String?,
        tribeServerIp: String?,
        mixerIp: String?
    ) {
        val now = getTimestampInMilliseconds()

        try {
            val createInvite = makeInvite(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                mixerIp ?: TEST_V2_SERVER_IP,
                convertSatsToMillisats(sats),
                ownerInfoStateFlow.value.alias ?: "",
                tribeServerIp,
                serverDefaultTribe,
                null, // needs to implement
                null // needs to implement
            )

            if (createInvite.newInvite != null) {
                val invite = createInvite.newInvite ?: return
                val code = codeFromInvite(invite)
                val tag = createInvite.msgs.getOrNull(0)?.tag

                currentInvite = NewInvite(
                    nickname,
                    sats,
                    welcomeMessage,
                    serverDefaultTribe,
                    invite,
                    code,
                    tag
                )

                handleRunReturn(createInvite)
            }
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.CreateInviteError)
            }
            LOG.d("MQTT_MESSAGES", "createInvite ${e.message}")
        }
    }

    override fun deleteInvite(inviteString: String) {
        try {
            val cancelInvite = cancelInvite(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                inviteString
            )
            handleRunReturn(cancelInvite)
        } catch (e: Exception) {
        }
    }

    override fun setReadMessage(contactPubKey: String, messageIndex: Long) {
        try {
            val contacts = listContacts(getCurrentUserState())
            LOG.d("MQTT_MESSAGES", "readMessage contacts ${contacts}")

            val readMessage = read(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                contactPubKey,
                messageIndex.toULong()
            )
            handleRunReturn(readMessage)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.ReadMessageError)
            }
            LOG.d("MQTT_MESSAGES", "readMessage ${e.message}")
        }
    }

    override fun getReadMessages() {
        try {
            val readMessages = getReads(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState()
            )
            handleRunReturn(readMessages)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.GetReadMessagesError)
            }
            LOG.d("MQTT_MESSAGES", "getReadMessages ${e.message}")
        }
    }

    override fun setMute(muteLevel: Int, contactPubKey: String) {
        try {
            val mute = mute(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                contactPubKey,
                muteLevel.toUByte()
            )
            handleRunReturn(mute)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SetMuteError)
            }
        }
    }

    override fun getMutedChats() {
        try {
            val mutedChats = getMutes(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState()
            )
            handleRunReturn(mutedChats)
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "getMutedChats ${e.message}")
        }
    }

    override fun addNodesFromResponse(nodesJson: String) {
        try {
            val addNodes = addNode(
                nodesJson
            )
            handleRunReturn(addNodes)
        } catch (e: Exception) {
//            notifyListeners {
//                onConnectManagerError(ConnectManagerError.AddNodesError)
//            }
            LOG.d("MQTT_MESSAGES", "addNodesFromResponse ${e.message}")
        }
    }

    override fun concatNodesFromResponse(
        nodesJson: String,
        routerPubKey: String,
        amount: Long
    ) {
        try {
            val concatNodes = concatRoute(
                getCurrentUserState(),
                nodesJson,
                routerPubKey,
                amount.toULong()
            )
            handleRunReturn(concatNodes)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.ConcatNodesError)
            }
            LOG.d("MQTT_MESSAGES", "concatNodesFromResponse ${e.message}")
        }
    }

    override fun fetchMessagesOnAppInit(
        lastMsgIdx: Long?,
        reverse: Boolean
    ) {
        try {
            val fetchMessages = fetchMsgsBatch(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                lastMsgIdx?.toULong() ?: 0.toULong(),
                MSG_BATCH_LIMIT.toUInt(),
                reverse
            )
            handleRunReturn(fetchMessages)

        } catch (e: Exception) {
//            notifyListeners {
//                onConnectManagerError(ConnectManagerError.FetchMessageError)
//            }
            LOG.d("MQTT_MESSAGES", "fetchMessagesOnAppInit ${e.message}")
        }
    }

    // Messaging Methods

    private fun handleMessageArrived(topic: String?, message: MqttMessage?) {
        if (!readyForPing && topic?.contains("ping") == true) {
            return
        }

        if (topic != null && message?.payload != null) {
            try {
                val runReturn = handle(
                    topic,
                    message.payload,
                    ownerSeed ?: "",
                    getTimestampInMilliseconds(),
                    getCurrentUserState(),
                    ownerInfoStateFlow.value.alias ?: "",
                    ownerInfoStateFlow.value.picture ?: ""
                )

                mqttClient?.let { client ->
                    handleRunReturn(
                        runReturn,
                        topic = topic
                    )
                }

                LOG.d("MQTT_MESSAGES", " this is handle ${runReturn}")

                runReturn.msgs.forEach {
                    LOG.d("RESTORE_MESSAGES", " ${it}")
                }

            } catch (e: Exception) {
                LOG.d("MQTT_MESSAGES", "handleMessageArrived ${e.message}")
            }
        }
    }

    override fun sendMessage(
        sphinxMessage: String,
        contactPubKey: String,
        provisionalId: Long,
        messageType: Int,
        amount: Long?,
        isTribe: Boolean
    ) {
        val now = getTimestampInMilliseconds()

        // Have to include al least 1 sat for tribe messages
        val nnAmount = when {
            isTribe && (amount == null || amount < 3L) -> 3L
            isTribe -> amount ?: 1L
            else -> amount ?: 0L
        }
        try {
            val message = send(
                ownerSeed!!,
                now,
                contactPubKey,
                messageType.toUByte(),
                sphinxMessage,
                getCurrentUserState(),
                ownerInfoStateFlow.value.alias ?: "",
                ownerInfoStateFlow.value.picture ?: "",
                convertSatsToMillisats(nnAmount),
                isTribe
            )
            handleRunReturn(message)

            message.msgs.firstOrNull()?.let { sentMessage ->
                sentMessage.uuid?.let { msgUuid ->
                    notifyListeners {
                        onMessageTagAndUuid(sentMessage.tag, msgUuid, provisionalId)
                    }
                }
            }

        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SendMessageError)
            }
            LOG.d("MQTT_MESSAGES", "send ${e.message}")
        }
    }

    override fun deleteMessage(
        sphinxMessage: String,
        contactPubKey: String,
        isTribe: Boolean
    ) {
        val now = getTimestampInMilliseconds()

        // Have to include al least 1 sat for tribe messages
        val nnAmount = if (isTribe) 1L else 0L

        try {
            val message = send(
                ownerSeed!!,
                now,
                contactPubKey,
                17.toUByte(), // Fix this hardcoded value
                sphinxMessage,
                getCurrentUserState(),
                ownerInfoStateFlow.value.alias ?: "",
                ownerInfoStateFlow.value.picture ?: "",
                convertSatsToMillisats(nnAmount),
                isTribe
            )
            handleRunReturn(message)

        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.DeleteMessageError)
            }
            LOG.d("MQTT_MESSAGES", "send ${e.message}")
        }
    }

    override fun deleteContactMessages(messageIndexList: List<Long>) {
        try {
            val deleteOkKeyMessages = deleteMsgs(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                null,
                messageIndexList.map { it.toULong() }
            )
            handleRunReturn(deleteOkKeyMessages)
        } catch (e: Exception) {
//            notifyListeners {
//                onConnectManagerError(ConnectManagerError.DeleteContactMessagesError)
//            }
            LOG.d("MQTT_MESSAGES", "deleteContactMessages ${e.message}")
        }
    }

    override fun deletePubKeyMessages(contactPubKey: String) {
        try {
            val deletePubKeyMsgs = deleteMsgs(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                contactPubKey,
                null
            )
            handleRunReturn(deletePubKeyMsgs)
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "deletePubKeyMessages ${e.message}")
        }
    }

    override fun getMessagesStatusByTags(tags: List<String>) {
        try {
            val messageStatus = getTags(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                tags,
                null
            )
            handleRunReturn(messageStatus)
        } catch (e: Exception) {
//            notifyListeners {
//                onConnectManagerError(ConnectManagerError.MessageStatusError)
//            }
            LOG.d("MQTT_MESSAGES", "getMessagesStatusByTags ${e.message}")
        }
    }

    // Tribe Management Methods
    override fun createTribe(tribeJson: String) {
        val now = getTimestampInMilliseconds()

        try {
            val tribeServerPubKey = getTribeServerPubKey()
            val createTribe = tribeServerPubKey?.let { tribePubKey ->
                uniffi.sphinxrs.createTribe(
                    ownerSeed!!,
                    now,
                    getCurrentUserState(),
                    tribePubKey,
                    tribeJson
                )
            }
            if (createTribe != null) {
                handleRunReturn(createTribe)
            }
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.CreateTribeError)
            }
            LOG.d("MQTT_MESSAGES", "createTribe ${e.message}")
        }
    }

    override fun joinToTribe(
        tribeHost: String,
        tribePubKey: String,
        tribeRouteHint: String,
        isPrivate: Boolean,
        userAlias: String,
        priceToJoin: Long
    ) {
        val now = getTimestampInMilliseconds()
        val amount = if (priceToJoin == 0L) 1L else priceToJoin

        try {
            val joinTribeMessage = joinTribe(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                tribePubKey,
                tribeRouteHint,
                userAlias,
                convertSatsToMillisats(amount),
                isPrivate
            )
            handleRunReturn(joinTribeMessage)

        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.JoinTribeError)
            }
            LOG.d("MQTT_MESSAGES", "joinTribe ${e.message}")
        }
    }

    override fun retrieveTribeMembersList(tribeServerPubKey: String, tribePubKey: String) {
        val now = getTimestampInMilliseconds()

        try {
            val tribeMembers = listTribeMembers(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                tribeServerPubKey,
                tribePubKey
            )
            handleRunReturn(tribeMembers)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.ListTribeMembersError)
            }
            LOG.d("MQTT_MESSAGES", "tribeMembers ${e.message}")
        }
    }

    override fun getTribeServerPubKey(): String? {
        return try {
            val defaultTribe = getDefaultTribeServer(
                getCurrentUserState()
            )
            LOG.d("MQTT_MESSAGES", "getDefaultTribeServer $defaultTribe")
            defaultTribe
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.ServerPubKeyError)
            }
            null
        }
    }

    override fun editTribe(tribeJson: String) {
        val tribeServerPubkey = getTribeServerPubKey()
        try {
            val updatedTribe = tribeServerPubkey?.let {
                updateTribe(
                    ownerSeed!!,
                    getTimestampInMilliseconds(),
                    getCurrentUserState(),
                    it,
                    tribeJson
                )
            }
            if (updatedTribe != null) {
                handleRunReturn(updatedTribe)
            }
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "editTribe ${e.message}")
        }
    }

    override fun createInvoice(amount: Long, memo: String): Pair<String, String>? {
        val now = getTimestampInMilliseconds()

        try {
            val makeInvoice = uniffi.sphinxrs.makeInvoice(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                convertSatsToMillisats(amount),
                memo
            )
            handleRunReturn(makeInvoice)

            val invoice = makeInvoice.invoice

            if (invoice != null) {
                val paymentHash = paymentHashFromInvoice(invoice)
                return Pair(invoice, paymentHash)
            }

        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.CreateInvoiceError)
            }
            LOG.d("MQTT_MESSAGES", "makeInvoice ${e.message}")
        }
        return null
    }

    override fun sendKeySend(pubKey: String, amount: Long, routeHint: String?, data: String?) {
        val now = getTimestampInMilliseconds()

        try {
            val keySend = uniffi.sphinxrs.keysend(
                ownerSeed!!,
                now,
                pubKey,
                getCurrentUserState(),
                convertSatsToMillisats(amount),
                data?.encodeToByteArray(),
                routeHint
            )
            handleRunReturn(keySend)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.SendKeySendError)
            }
            LOG.d("MQTT_MESSAGES", "sendKeySend ${e.message}")
        }
    }

    override fun processContactInvoicePayment(paymentRequest: String) {
        val now = getTimestampInMilliseconds()

        try {
            val processInvoice = uniffi.sphinxrs.payContactInvoice(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                paymentRequest,
                ownerInfoStateFlow.value.alias ?: "",
                ownerInfoStateFlow.value.picture ?: "",
                false // not implemented on tribes yet
            )
            handleRunReturn(processInvoice)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.PayContactInvoiceError)
            }
            LOG.d("MQTT_MESSAGES", "processInvoicePayment ${e.message}")
        }
    }

    override fun processInvoicePayment(
        paymentRequest: String,
        milliSatAmount: Long
    ): String? {
        try {
            val invoice = payInvoice(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                paymentRequest,
                milliSatAmount.toULong()
            )
            handleRunReturn(invoice)
            return invoice.msgs.first().tag
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.PayInvoiceError)
            }
            LOG.d("MQTT_MESSAGES", "processInvoicePayment ${e.message}")
            return null
        }
    }

    override fun payInvoiceFromLSP(paymentRequest: String) {
        try {
            val invoice = pay(
                ownerSeed!!,
                getTimestampInMilliseconds(),
                getCurrentUserState(),
                paymentRequest
            )
            handleRunReturn(invoice)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.PayInvoiceError)
            }
            LOG.d("MQTT_MESSAGES", "payInvoiceFromLSP ${e.message}")
        }
    }


    override fun retrievePaymentHash(paymentRequest: String): String? {
        return try {
            paymentHashFromInvoice(paymentRequest)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.PaymentHashError)
            }
            null
        }
    }

    override fun getPayments(
        lastMsgDate: Long,
        limit: Int,
        scid: Long?,
        remoteOnly: Boolean?,
        minMsat: Long?,
        reverse: Boolean?
    ) {
        val now = getTimestampInMilliseconds()

        try {
            val payments = uniffi.sphinxrs.fetchPayments(
                ownerSeed!!,
                now,
                getCurrentUserState(),
                lastMsgDate.plus(1000).toULong(),
                limit.toUInt(),
                scid?.toULong(),
                remoteOnly ?: false,
                minMsat?.toULong(),
                true
            )
            handleRunReturn(payments)
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.LoadTransactionsError)
            }
            LOG.d("MQTT_MESSAGES", "getPayments ${e.message}")
        }
    }

    override fun getPubKeyByEncryptedChild(
        child: String,
        pushKey: String?
    ): String? {
        var childIndex: ULong? = null
        var publicKey: String? = null

        pushKey?.let {
            try {
                childIndex = uniffi.sphinxrs.decryptChildIndex(
                    child,
                    it
                )
            } catch (e: Exception) {
                return null
            }
        }

        childIndex?.let {
            try {
                publicKey = uniffi.sphinxrs.contactPubkeyByChildIndex(
                    getCurrentUserState(),
                    it
                )
            } catch (e: Exception) {
                return null
            }
        }

        return publicKey
    }

    override fun generateMediaToken(
        contactPubKey: String,
        muid: String,
        host: String,
        metaData: String?,
        amount: Long?
    ): String? {
        val now = getTimestampInMilliseconds()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.YEAR, 1)

        val yearFromNow = try {
            (calendar.timeInMillis / 1000).toUInt()
        } catch (e: Exception) {
            null
        }

        return try {
            if (amount != null && amount > 0) {
                makeMediaTokenWithPrice(
                    ownerSeed!!,
                    now,
                    getCurrentUserState(),
                    host,
                    muid,
                    contactPubKey,
                    yearFromNow!!,
                    amount.toULong(),
                )
            } else {
                if (metaData != null) {
                    makeMediaTokenWithMeta(
                        ownerSeed!!,
                        now,
                        getCurrentUserState(),
                        host,
                        muid,
                        contactPubKey,
                        yearFromNow!!,
                        metaData
                    )
                } else {
                    makeMediaToken(
                        ownerSeed!!,
                        now,
                        getCurrentUserState(),
                        host,
                        muid,
                        contactPubKey,
                        yearFromNow!!
                    )
                }
            }
        } catch (e: Exception) {
            notifyListeners {
                onConnectManagerError(ConnectManagerError.MediaTokenError)
            }
            LOG.d("MQTT_MESSAGES", "Error to generate media token $e")
            null
        }
    }

    override fun getInvoiceInfo(invoice: String): String? {
        return try {
            parseInvoice(invoice)
        } catch (e: Exception) {
            null
        }
    }

    override fun isRouteAvailable(pubKey: String, routeHint: String?, milliSat: Long): Boolean {
        return try {
            findRoute(
                getCurrentUserState(),
                pubKey,
                routeHint,
                milliSat.toULong()
            )
            true
        } catch (e: java.lang.Exception) {
            false
        }
    }

    override fun getSignedTimeStamps(): String? {
        return try {
            signedTimestamp(
                ownerSeed!!,
                0.toULong(),
                getTimestampInMilliseconds(),
                network
            )
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "Error to get signed timestamp $e")
            null
        }
    }

    override fun getSignBase64(text: String): String? {
        return try {
            signBase64(
                ownerSeed!!,
                0.toULong(),
                getTimestampInMilliseconds(),
                network,
                text
            )
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "Error to get sign base64 $e")
            null
        }
    }

    override fun getIdFromMacaroon(macaroon: String): String? {
        return try {
            idFromMacaroon(macaroon)
        } catch (e: Exception) {
            LOG.d("MQTT_MESSAGES", "Error to get id from macaroon $e")
            null
        }
    }

    private fun publishTopicsSequentially(topics: Array<String>, messages: Array<String>?, index: Int) {
        if (index < topics.size) {
            val topic = topics[index]
            val mqttMessage = messages?.getOrNull(index)

            val message = if (mqttMessage?.isNotEmpty() == true) {
                MqttMessage(mqttMessage.toByteArray())
            } else {
                MqttMessage()
            }

            mqttClient?.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    // Recursively call the function with the next index
                    publishTopicsSequentially(topics, messages, index + 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    LOG.d("MQTT_MESSAGES", "Failed to publish to $topic: ${exception?.message}")
                }
            })
        }
    }

    // Utility Methods
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun generateRandomBytes(size: Int): UByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        val uByteArray = UByteArray(size)

        for (i in bytes.indices) {
            uByteArray[i] = bytes[i].toUByte()
        }

        return uByteArray
    }

    private fun getTimestampInMilliseconds(): String =
        System.currentTimeMillis().toString()

    private fun handleSettledStatus(settledStatus: String?) {
        settledStatus?.let { status ->
            try {
                val data = status.toByteArray(Charsets.UTF_8)
                val dictionary = JSONObject(String(data))
                val htlcId = dictionary.getString("htlc_id")
                val settleStatus = dictionary.getString("status")

                if (settleStatus == COMPLETE_STATUS) {
                    val rrObject = delayedRRObjects.firstOrNull { it.msgs.any { msg -> msg.index == htlcId } }

                    delayedRRObjects = delayedRRObjects.filter { rr ->
                        rr.msgs.none { msg -> msg.index == htlcId }
                    }.toMutableList()

                    rrObject?.let { rr ->
                        handleRunReturn(
                            rr,
                            true
                        )
                    }
                } else {
                    // Show toast error
                }
            } catch (e: JSONException) {
                LOG.d("MQTT_MESSAGES", "Error decoding JSON: ${e.message}")
            }
        }
    }

    private fun logLongString(tag: String, str: String) {
        val maxLogSize = 4000
        for (i in 0..str.length / maxLogSize) {
            val start = i * maxLogSize
            val end = (i + 1) * maxLogSize
            LOG.d(tag, str.substring(start, min(end, str.length)))
        }
    }

    private fun resetMQTT() {
        if (mqttClient?.isConnected == true) {
            mqttClient?.disconnect()
        }
    }

    private fun storeUserStateOnSharedPreferences(newUserState: MutableMap<String, ByteArray>) {
        val existingUserState = retrieveUserStateMap(ownerInfoStateFlow.value.userState)
        existingUserState.putAll(newUserState)

        val encodedString = encodeMapToBase64(existingUserState)

        // Update class var
        _ownerInfoStateFlow.value = ownerInfoStateFlow.value.copy(
            userState = encodedString
        )

        // Store on serverURl
    }

    private fun retrieveUserStateMap(encodedString: String?): MutableMap<String, ByteArray> {
        val result = encodedString?.let {
            decodeBase64ToMap(it)
        } ?: mutableMapOf()

        return result
    }

    private fun getCurrentUserState(): ByteArray {
        val userStateMap = retrieveUserStateMap(ownerInfoStateFlow.value.userState)
        LOG.d("MQTT_MESSAGES", "getCurrentUserState $userStateMap")

        return MsgPack.encodeToByteArray(MsgPackDynamicSerializer, userStateMap)
    }

    private fun encodeMapToBase64(map: MutableMap<String, ByteArray>): String {
        val encodedMap = mutableMapOf<String, String>()

        for ((key, value) in map) {
            encodedMap[key] = Base64.getEncoder().withoutPadding().encodeToString(value)
        }

        val result = (encodedMap as Map<*, *>?)?.let { JSONObject(it).toString() } ?: ""


        return result
    }

    private fun extractTagAndStatus(sentStatusJson: String?): Pair<String, Boolean>? {
        if (sentStatusJson == null) return null

        try {
            val jsonObject = JSONObject(sentStatusJson)
            val tag = jsonObject.getString("tag")
            val status = jsonObject.getString("status") == "COMPLETE"

            return Pair(tag, status)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun decodeBase64ToMap(encodedString: String): MutableMap<String, ByteArray> {
        if (encodedString.isEmpty()) {
            return mutableMapOf()
        }

        val decodedMap = mutableMapOf<String, ByteArray>()

        try {
            val jsonObject = JSONObject(encodedString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val encodedValue = jsonObject.getString(key.toString())
                val decodedValue = Base64.getDecoder().decode(encodedValue)
                decodedMap[key.toString()] = decodedValue
            }
        } catch (e: JSONException) {
        }

        return decodedMap
    }

    private fun convertSatsToMillisats(sats: Long): ULong {
        return (sats * 1_000).toULong()
    }

    fun convertMillisatsToSats(millisats: ULong): Long? {
        try {
            return (millisats / 1_000uL).toLong()
        } catch (e: Exception) {
            return null
        }
    }

    private fun extractTribeServerAndPubkey(url: String): Pair<String, String>? {
        val regex = Regex("https://([^/]+)/tribes/([^/]+)")
        return regex.find(url)?.groupValues?.takeIf { it.size == 3 }?.let {
            it[1] to it[2]
        }
    }

    private fun isConnected(): Boolean {
        return isMqttConnected
    }

    private fun handlePing(ping: String) {
        val parts = ping.split(":")
        if (parts.size > 1) {
            val paymentHash = parts[0]
            val timestamp = parts[1].toLong()

            if (paymentHash != "_") {
                pingsMap[paymentHash] = timestamp
            }

            if (parts.size > 2) {
                val tag = parts[2]
                pingsMap[tag] = timestamp
            }
        } else {
            LOG.d("MQTT_MESSAGE", "Invalid ping format")
        }
    }

    private fun handlePingDone(msgs: List<Msg>) {
        msgs.filter { it.paymentHash?.isNotEmpty() == true }
            .mapNotNull { it.paymentHash }
            .forEach { paymentHash ->
                pingsMap[paymentHash]?.let { timestamp ->
                    try {
                        val pingDone = pingDone(
                            ownerSeed!!,
                            getTimestampInMilliseconds(),
                            getCurrentUserState(),
                            timestamp.toULong()
                        )
                        handleRunReturn(pingDone)
                        removeFromPingsMapWith(paymentHash)
                    } catch (e: Exception) {
                        LOG.d("MQTT_MESSAGES", "Error calling ping done")
                    }
                }
            }
    }

    private fun handleError(error: String) {
        LOG.d("MQTT_MESSAGES", "=> error $error")

        if (error.contains("async pay not found")) {
            pingsMap.keys.forEach { tag ->
                if (error.contains(tag)) {
                    pingsMap[tag]?.let { timestamp ->
                        try {
                            val pingDone = pingDone(
                                ownerSeed!!,
                                getTimestampInMilliseconds(),
                                getCurrentUserState(),
                                timestamp.toULong()
                            )
                            handleRunReturn(pingDone)
                            removeFromPingsMapWith(tag)
                        } catch (e: Exception) {
                            LOG.d("MQTT_MESSAGES", "Error calling ping done")
                        }
                    }
                }
            }
        }
    }

    private fun removeFromPingsMapWith(key: String) {
        pingsMap[key]?.let { timestamp ->
            pingsMap.filter { it.value == timestamp }.forEach { mapEntry ->
                pingsMap.remove(mapEntry.key)
            }
        }
    }

    // Listener Methods
    private val synchronizedListeners = SynchronizedListenerHolder()

    override fun addListener(listener: ConnectManagerListener): Boolean {
        return synchronizedListeners.addListener(listener)
    }

    override fun removeListener(listener: ConnectManagerListener): Boolean {
        return synchronizedListeners.removeListener(listener)
    }

    private fun notifyListeners(action: ConnectManagerListener.() -> Unit) {
        synchronizedListeners.forEachListener { listener ->
            action(listener)
        }
    }

    private fun isRestoreAccount(): Boolean = restoreMnemonicWords?.isNotEmpty() == true

    private inner class SynchronizedListenerHolder {
        private val listeners: LinkedHashSet<ConnectManagerListener> = LinkedHashSet()

        fun addListener(listener: ConnectManagerListener): Boolean = synchronized(this) {
            listeners.add(listener).also {
                if (it) {
                    // Log listener registration
                }
            }
        }

        fun removeListener(listener: ConnectManagerListener): Boolean = synchronized(this) {
            listeners.remove(listener).also {
                if (it) {
                    // Log listener removal
                }
            }
        }

        fun forEachListener(action: (ConnectManagerListener) -> Unit) {
            synchronized(this) {
                listeners.forEach(action)
            }
        }
    }

    private fun setContactKeyTotal(firstForEachScid: Long?) {
        firstForEachScid?.let {
            restoreProgress.totalContactsKey = it.toInt()
        }
    }

    private fun setMessagesTotal(totalHighestIndex: Long?) {
        totalHighestIndex?.let {
            restoreProgress.totalMessages = it.toInt()
        }
    }

    private fun calculateContactRestore() {
        try {
            val newAmountOfContacts = restoreProgress.contactsRestoredAmount.plus(MSG_FIRST_PER_KEY_LIMIT)
            if (newAmountOfContacts <= restoreProgress.totalContactsKey) {
                restoreProgress.contactsRestoredAmount = newAmountOfContacts
                restoreProgress.progressPercentage =
                    ((restoreProgress.contactsRestoredAmount.toDouble() / restoreProgress.totalContactsKey.toDouble()) * restoreProgress.fixedContactPercentage.toDouble()).roundToInt()
            } else {
                restoreProgress.contactsRestoredAmount = restoreProgress.totalContactsKey
                restoreProgress.progressPercentage = restoreProgress.fixedContactPercentage
            }
            notifyListeners {
                onRestoreProgress(restoreProgress.progressPercentage)
            }
        } catch (e: Exception) {
        }
    }

    private fun calculateMessageRestore() {
        try {
            val restoredMsgs = restoreProgress.restoredMessagesAmount.plus(MSG_BATCH_LIMIT)
            if (restoredMsgs >= restoreProgress.totalMessages) {
                restoreProgress.progressPercentage = 100
            } else {
                restoreProgress.restoredMessagesAmount = restoredMsgs
                restoreProgress.progressPercentage =
                    (restoreProgress.fixedContactPercentage + ((restoredMsgs.toDouble() / restoreProgress.totalMessages.toDouble())) * restoreProgress.fixedMessagesPercentage.toDouble()).roundToInt()
            }
            notifyListeners {
                onRestoreProgress(restoreProgress.progressPercentage)
            }
        } catch (e: Exception) {
        }
    }
}