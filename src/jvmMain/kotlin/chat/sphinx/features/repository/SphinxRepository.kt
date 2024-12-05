package chat.sphinx.features.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import chat.sphinx.concepts.repository.connect_manager.model.NetworkStatus
import chat.sphinx.concepts.repository.connect_manager.model.OwnerRegistrationState
import chat.sphinx.concepts.repository.connect_manager.model.RestoreProcessState
import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.connect_manager.ConnectManager
import chat.sphinx.concepts.connect_manager.ConnectManagerListener
import chat.sphinx.concepts.connect_manager.model.OwnerInfo
import chat.sphinx.concepts.coredb.CoreDB
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.media_cache.MediaCacheHandler
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.chat.model.*
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.contact.model.*
import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.feed_search.model.toFeedSearchResult
import chat.sphinx.concepts.network.query.lightning.model.lightning.*
import chat.sphinx.concepts.network.query.meme_server.NetworkQueryMemeServer
import chat.sphinx.concepts.network.query.message.model.*
import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.network.query.redeem_badge_token.model.RedeemBadgeTokenDto
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.save_profile.model.DeletePeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.PeopleProfileDto
import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.notification.SphinxNotificationManager
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.concepts.repository.chat.ChatRepository
import chat.sphinx.concepts.repository.chat.model.CreateTribe
import chat.sphinx.concepts.repository.connect_manager.ConnectManagerRepository
import chat.sphinx.concepts.repository.contact.ContactRepository
import chat.sphinx.concepts.repository.dashboard.RepositoryDashboard
import chat.sphinx.concepts.repository.feed.FeedRepository
import chat.sphinx.concepts.repository.lightning.LightningRepository
import chat.sphinx.concepts.repository.media.RepositoryMedia
import chat.sphinx.concepts.repository.message.MessageRepository
import chat.sphinx.concepts.repository.message.model.AttachmentInfo
import chat.sphinx.concepts.repository.message.model.SendMessage
import chat.sphinx.concepts.repository.message.model.SendPayment
import chat.sphinx.concepts.repository.message.model.SendPaymentRequest
import chat.sphinx.concepts.repository.subscription.SubscriptionRepository
import chat.sphinx.crypto.common.annotations.RawPasswordAccess
import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import chat.sphinx.crypto.common.clazzes.*
import chat.sphinx.database.core.*
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.features.repository.mappers.chat.ChatDboPresenterMapper
import chat.sphinx.features.repository.mappers.contact.ContactDboPresenterMapper
import chat.sphinx.features.repository.mappers.feed.FeedDboPresenterMapper
import chat.sphinx.features.repository.mappers.feed.FeedDestinationDboPresenterMapper
import chat.sphinx.features.repository.mappers.feed.FeedItemDboPresenterMapper
import chat.sphinx.features.repository.mappers.feed.FeedModelDboPresenterMapper
import chat.sphinx.features.repository.mappers.feed.podcast.*
import chat.sphinx.features.repository.mappers.invite.InviteDboPresenterMapper
import chat.sphinx.features.repository.mappers.mapListFrom
import chat.sphinx.features.repository.mappers.message.MessageDboPresenterMapper
import chat.sphinx.features.repository.mappers.subscription.SubscriptionDboPresenterMapper
import chat.sphinx.features.repository.model.message.MessageDboWrapper
import chat.sphinx.features.repository.model.message.MessageMediaDboWrapper
import chat.sphinx.features.repository.model.message.convertMessageDboToNewMessage
import chat.sphinx.features.repository.util.*
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.logger.e
import chat.sphinx.logger.w
import chat.sphinx.response.*
import chat.sphinx.utils.ServersUrlsHelper
import chat.sphinx.utils.SphinxJson
import chat.sphinx.wrapper.*
import chat.sphinx.wrapper.chat.*
import chat.sphinx.wrapper.contact.*
import chat.sphinx.wrapper.dashboard.*
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.invite.Invite
import chat.sphinx.wrapper.invite.InviteCode
import chat.sphinx.wrapper.invite.InviteStatus
import chat.sphinx.wrapper.invite.InviteString
import chat.sphinx.wrapper.lightning.*
import chat.sphinx.wrapper.meme_server.PublicAttachmentInfo
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.Message
import chat.sphinx.wrapper.message.Msg.Companion.toMsg
import chat.sphinx.wrapper.message.MsgSender.Companion.toMsgSender
import chat.sphinx.wrapper.message.MsgSender.Companion.toMsgSenderNull
import chat.sphinx.wrapper.message.media.*
import chat.sphinx.wrapper.message.media.token.MediaHost
import chat.sphinx.wrapper.message.media.token.toMediaUrlOrNull
import chat.sphinx.wrapper.mqtt.*
import chat.sphinx.wrapper.mqtt.LastReadMessages.Companion.toLastReadMap
import chat.sphinx.wrapper.mqtt.MsgsCounts.Companion.toMsgsCounts
import chat.sphinx.wrapper.mqtt.MuteLevels.Companion.toMuteLevelsMap
import chat.sphinx.wrapper.mqtt.NewCreateTribe.Companion.toNewCreateTribe
import chat.sphinx.wrapper.mqtt.NewSentStatus.Companion.toNewSentStatus
import chat.sphinx.wrapper.mqtt.Payment.Companion.toPaymentsList
import chat.sphinx.wrapper.mqtt.TagMessageList.Companion.toTagsList
import chat.sphinx.wrapper.mqtt.TransactionDto
import chat.sphinx.wrapper.mqtt.TribeMembersResponse.Companion.toTribeMembersList
import chat.sphinx.wrapper.payment.PaymentTemplate
import chat.sphinx.wrapper.podcast.FeedSearchResultRow
import chat.sphinx.wrapper.podcast.Podcast
import chat.sphinx.wrapper.relay.*
import chat.sphinx.wrapper.rsa.RsaPrivateKey
import chat.sphinx.wrapper.rsa.RsaPublicKey
import chat.sphinx.wrapper.subscription.EndNumber
import chat.sphinx.wrapper.subscription.Subscription
import chat.sphinx.wrapper.subscription.SubscriptionId
import chat.sphinx.wrapper_chat.NotificationLevel
import chat.sphinx.wrapper_chat.isMuteChat
import chat.sphinx.wrapper_message.ThreadUUID
import chat.sphinx.wrapper_message.toThreadUUID
import com.soywiz.klock.DateTimeTz
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.matthewnelson.component.base64.encodeBase64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.absoluteValue

abstract class SphinxRepository(
    override val accountOwner: StateFlow<Contact?>,
    private val applicationScope: CoroutineScope,
    private val authenticationCoreManager: AuthenticationCoreManager,
    private val authenticationStorage: AuthenticationStorage,
    private val relayDataHandler: RelayDataHandler,
    private val coreDB: CoreDB,
    private val dispatchers: CoroutineDispatchers,
    private val mediaCacheHandler: MediaCacheHandler,
    private val memeInputStreamHandler: MemeInputStreamHandler,
    private val memeServerTokenHandler: MemeServerTokenHandler,
    private val networkQueryMemeServer: NetworkQueryMemeServer,
    private val networkQueryChat: NetworkQueryChat,
    private val networkQueryContact: NetworkQueryContact,
    private val networkQueryAuthorizeExternal: NetworkQueryAuthorizeExternal,
    private val networkQuerySaveProfile: NetworkQuerySaveProfile,
    private val networkQueryRedeemBadgeToken: NetworkQueryRedeemBadgeToken,
    private val networkQueryFeedSearch: NetworkQueryFeedSearch,
    private val connectManager: ConnectManager,
    private val rsa: RSA,
    private val sphinxNotificationManager: SphinxNotificationManager,
    protected val LOG: SphinxLogger,
) : ChatRepository,
    ContactRepository,
    LightningRepository,
    MessageRepository,
    SubscriptionRepository,
    RepositoryDashboard,
    RepositoryMedia,
    FeedRepository,
    ConnectManagerRepository,
    CoroutineDispatchers by dispatchers,
    ConnectManagerListener
{

    companion object {
        const val TAG: String = "SphinxRepository"

        // PersistentStorage Keys
        const val REPOSITORY_LIGHTNING_BALANCE = "REPOSITORY_LIGHTNING_BALANCE"
        const val REPOSITORY_LAST_SEEN_MESSAGE_DATE = "REPOSITORY_LAST_SEEN_MESSAGE_DATE"
        const val REPOSITORY_LAST_SEEN_CONTACTS_DATE = "REPOSITORY_LAST_SEEN_CONTACTS_DATE"
        const val REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE =
            "REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE"

        // networkRefreshMessages
        const val MESSAGE_PAGINATION_LIMIT = 200
        const val DATE_NIXON_SHOCK = "1971-08-15T00:00:00.000Z"

        const val MEDIA_KEY_SIZE = 32
        const val MEDIA_PROVISIONAL_TOKEN = "Media_Provisional_Token"

        const val AUTHORIZE_EXTERNAL_BASE_64 = "U3BoaW54IFZlcmlmaWNhdGlvbg=="
    }

    private val serversUrls = ServersUrlsHelper()

    ////////////////////////
    /// Connect Manager ///
    //////////////////////


    override val connectionManagerState: MutableStateFlow<OwnerRegistrationState?> by lazy {
        MutableStateFlow(null)
    }

    override val networkStatus: MutableStateFlow<NetworkStatus> by lazy {
        MutableStateFlow(NetworkStatus.Loading)
    }

    override val restoreProcessState: MutableStateFlow<RestoreProcessState?> by lazy {
        MutableStateFlow(null)
    }

    override val connectManagerErrorState: MutableStateFlow<ConnectManagerError?> by lazy {
        MutableStateFlow(null)
    }

    override val transactionDtoState: MutableStateFlow<List<TransactionDto>?> by lazy {
        MutableStateFlow(null)
    }

    override val userStateFlow: MutableStateFlow<String?> by lazy {
        MutableStateFlow(null)
    }

    override val tribeMembersState: MutableStateFlow<TribeMembersResponse?> by lazy {
        MutableStateFlow(null)
    }

    override val restoreProgress: MutableStateFlow<RestoreProgress?> by lazy {
        MutableStateFlow(null)
    }

    override val webViewPaymentHash: MutableStateFlow<String?> by lazy {
        MutableStateFlow(null)
    }

    override val processingInvoice: MutableStateFlow<Pair<String, String>?> by lazy {
        MutableStateFlow(null)
    }

    override val webViewPreImage: MutableStateFlow<String?> by lazy {
        MutableStateFlow(null)
    }

    override val restoreMinIndex: MutableStateFlow<Long?> by lazy {
        MutableStateFlow(null)
    }

    override val mnemonicWords: MutableStateFlow<String?> by lazy {
        MutableStateFlow(null)
    }

    override val profileSetInfoRestore: MutableStateFlow<Boolean?> by lazy {
        MutableStateFlow(null)
    }

    override fun setInviteCode(inviteString: String) {
        connectManager.setInviteCode(inviteString)
    }

    override fun setMnemonicWords(words: List<String>?) {
        connectManager.setMnemonicWords(words)
    }

    override fun setNetworkType(isTestEnvironment: Boolean) {
        connectManager.setNetworkType(isTestEnvironment)
    }

    override fun signChallenge(challenge: String): String? {
        return connectManager.processChallengeSignature(challenge)
    }

    override fun createInvite(
        nickname: String,
        welcomeMessage: String,
        sats: Long
    ) {
        val serverDefaultTribe = serversUrls.getDefaultTribe()?.ifEmpty { null }
        val tribeServerIp = serversUrls.getTribeServerIp()?.ifEmpty { null }
        val mixerIp = serversUrls.getNetworkMixerIp()?.ifEmpty { null }

        connectManager.createInvite(
            nickname,
            welcomeMessage,
            sats,
            serverDefaultTribe,
            tribeServerIp,
            mixerIp
        )
    }

    override fun joinTribe(
        tribeHost: String,
        tribePubKey: String,
        tribeRouteHint: String,
        tribeName: String,
        tribePicture: String?,
        isPrivate: Boolean,
        userAlias: String,
        pricePerMessage: Long,
        escrowAmount: Long,
        priceToJoin: Long
    ) {
        connectManager.joinToTribe(
            tribeHost,
            tribePubKey,
            tribeRouteHint,
            isPrivate,
            userAlias,
            priceToJoin
        )

        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            // TribeId is set from LONG.MAX_VALUE and decremented by 1 for each new tribe
            val tribeId = queries.chatGetLastTribeId().executeAsOneOrNull()?.let { it.MIN?.minus(1) }
                ?: (Long.MAX_VALUE)
            val now: String = DateTime.nowUTC()

            val newTribe = Chat(
                id = ChatId(tribeId),
                uuid = ChatUUID(tribePubKey),
                name = ChatName( tribeName ?: "unknown"),
                photoUrl = tribePicture?.toPhotoUrl(),
                type = ChatType.Tribe,
                status = ChatStatus.Approved,
                contactIds = listOf(ContactId(0), ContactId(tribeId)),
                isMuted = ChatMuted.False,
                createdAt = now.toDateTime(),
                groupKey = null,
                host = ChatHost(tribeHost),
                pricePerMessage = pricePerMessage.toSat(),
                escrowAmount = escrowAmount.toSat(),
                unlisted = ChatUnlisted.False,
                privateTribe = ChatPrivate.False,
                ownerPubKey = LightningNodePubKey(tribePubKey),
                seen = Seen.False,
                metaData = null,
                myPhotoUrl = null,
                myAlias = userAlias.toChatAlias(),
                pendingContactIds = emptyList(),
                latestMessageId = null,
                contentSeenAt = null,
                notify = NotificationLevel.SeeAll,
                secondBrainUrl = null
            )

            chatLock.withLock {
                queries.transaction {
                    upsertNewChat(
                        newTribe,
                        SynchronizedMap<ChatId, Seen>(),
                        queries,
                        null,
                        accountOwner.value?.nodePubKey
                    )
                }
            }
        }
    }

    override fun getTribeMembers(tribeServerPubKey: String, tribePubKey: String) {
        connectManager.retrieveTribeMembersList(tribeServerPubKey, tribePubKey)
    }

    override fun getTribeServerPubKey(): String? {
        return connectManager.getTribeServerPubKey()
    }

    override fun getPayments(lastMessageDate: Long, limit: Int) {
        connectManager.getPayments(
            lastMessageDate,
            limit,
            null,
            null,
            null,
            true
        )
    }

    override fun getTagsByChatId(chatId: ChatId) {
        applicationScope.launch(io) {
            getSentConfirmedMessagesByChatId(chatId).collect { messages ->
                if (messages.isNotEmpty()) {
                    val tags = messages.mapNotNull { it.tagMessage?.value }.distinct()
                    connectManager.getMessagesStatusByTags(tags)
                }
            }
        }
    }

    override suspend fun payContactPaymentRequest(
        paymentRequest: LightningPaymentRequest?
    ) {
        applicationScope.launch(mainImmediate) {
            paymentRequest?.value?.let {
                connectManager.processContactInvoicePayment(it)
            }
        }
    }

    override suspend fun payInvoice(
        paymentRequest: LightningPaymentRequest,
        endHops: String?,
        routerPubKey: String?,
        milliSatAmount: Long,
        isSphinxInvoice: Boolean,
        paymentHash: String?,
        callback: (() -> Unit)?
    ) {
        if (paymentHash != null) {
            webViewPaymentHash.value = paymentHash
        }

        if (endHops?.isNotEmpty() == true && routerPubKey != null) {
            connectManager.concatNodesFromResponse(
                endHops,
                routerPubKey,
                milliSatAmount
            )
        }
        val tag = connectManager.processInvoicePayment(
            paymentRequest.value,
            milliSatAmount
        )
        if (!isSphinxInvoice) {
            tag?.let {
                callback?.invoke()
                processingInvoice.value = Pair(paymentRequest.value, it)

                val timer = Timer("OneTimeTimer", true)
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        processingInvoice.value = null
                    }
                }, 60000)
            }
        }
    }

    override suspend fun payInvoiceFromLSP(paymentRequest: LightningPaymentRequest) {
        connectManager.payInvoiceFromLSP(paymentRequest.value)
    }

    override fun isRouteAvailable(pubKey: String, routeHint: String?, milliSat: Long): Boolean {
        return connectManager.isRouteAvailable(pubKey, routeHint, milliSat)
    }

    override fun createInvoice(amount: Long, memo: String): Pair<String, String>? {
        return connectManager.createInvoice(amount, memo)
    }

    override fun getInvoiceInfo(invoice: String): String? {
        return connectManager.getInvoiceInfo(invoice)
    }

    override fun cancelRestore() {
        onRestoreFinished(isRestoreCancelled = true)
    }

    override fun reconnectMqtt() {
        applicationScope.launch(mainImmediate) {
            delay(1000L)
            connectManager.reconnectWithBackOff()
        }
    }

    override fun cleanMnemonic() {
        mnemonicWords.value = null
    }

    override fun disconnectMqtt() {
        connectManager.disconnectMqtt()
    }

    override fun connectAndSubscribeToMqtt() {
        applicationScope.launch(mainImmediate) {
            val userState = serversUrls.getUserState()
            val mixerIp = serversUrls.getNetworkMixerIp()
            val queries = coreDB.getSphinxDatabaseQueries()
            val mnemonic = relayDataHandler.retrieveWalletMnemonic()
            var owner: Contact? = accountOwner.value

            if (owner == null) {
                try {
                    accountOwner.collect { contact ->
                        if (contact != null) {
                            owner = contact
                            throw Exception()
                        }
                    }
                } catch (e: Exception) {
                }
                delay(25L)
            }

            val okKey = owner?.nodePubKey?.value
            val lastMessageIndex = queries.messageGetMaxId().executeAsOneOrNull()?.MAX

            val ownerInfo = OwnerInfo(
                owner?.alias?.value ?: "",
                owner?.photoUrl?.value ?: "",
                userState,
                lastMessageIndex
            )

            if (mnemonic != null && okKey != null && mixerIp != null) {
                connectManager.initializeMqttAndSubscribe(
                    mixerIp,
                    mnemonic,
                    ownerInfo
                )
            } else {
                val logMixerIp = !mixerIp.isNullOrEmpty()
                val logMnemonic = !mnemonic?.value.isNullOrEmpty()
                val logOkKey = !okKey.isNullOrEmpty()

                connectManagerErrorState.value = ConnectManagerError.MqttInitError(
                    "mixerIp: $logMixerIp mnemonic: $logMnemonic okKey: $logOkKey"
                )
            }
        }

    }

    override fun createOwnerAccount(ownerAlias: String) {
        connectManager.createAccount(ownerAlias)
    }

    override fun startRestoreProcess() {
        applicationScope.launch(mainImmediate) {
            var msgCounts: MsgsCounts? = null

            restoreProcessState.asStateFlow().collect{ restoreProcessState ->
                when (restoreProcessState) {
                    is RestoreProcessState.MessagesCounts -> {
                        msgCounts = restoreProcessState.msgsCounts
                        connectManager.fetchFirstMessagesPerKey(0L, msgCounts?.first_for_each_scid)
                    }
                    is RestoreProcessState.RestoreMessages -> {
                        delay(100L)
                        connectManager.fetchMessagesOnRestoreAccount(
                            msgCounts?.total_highest_index,
                            msgCounts?.total
                        )

                    }
                    else -> {}
                }
            }
        }
    }

    override fun createContact(contact: NewContact) {
        applicationScope.launch(mainImmediate) {
            createNewContact(contact)
            connectManager.createContact(contact)
            println("CREATE_CONTACT: createContact (SPHINX REPO)")
        }
    }

    // ConnectManagerListener Callbacks implemented
    init {
        connectManager.addListener(this)
        memeServerTokenHandler.addListener(this)
    }

    // Account Management
    override fun onUpdateUserState(userState: String) {
        serversUrls.storeUserState(userState)
    }

    override fun onMnemonicWords(words: String, isRestore: Boolean) {
        applicationScope.launch(io) {
            words.toWalletMnemonic()?.let {
                relayDataHandler.persistWalletMnemonic(it)
            }
        }
        if (!isRestore) {
            mnemonicWords.value = words
        }
    }

    override fun onOwnerRegistered(
        okKey: String,
        routeHint: String,
        isRestoreAccount: Boolean,
        mixerServerIp: String?,
        tribeServerHost: String?,
        isProductionEnvironment: Boolean,
        routerUrl: String?,
        defaultTribe: String?,
        ownerAlias: String?
    ) {

        applicationScope.launch(mainImmediate) {
            val scid = routeHint.toLightningRouteHint()?.getScid()

            if (scid != null) {
                val alias = if(!isRestoreAccount) ownerAlias else null

                if (accountOwner.value?.nodePubKey == null) {
                    createOwner(okKey, routeHint, scid, alias)
                }

                mixerServerIp?.let { serversUrls.storeNetworkMixerIp(it) }
                defaultTribe?.let { serversUrls.storeDefaultTribe(it) }
                serversUrls.storeEnvironmentType(isProductionEnvironment)

                val needsToFetchConfig = routerUrl.isNullOrEmpty() || tribeServerHost.isNullOrEmpty()

                if (needsToFetchConfig) {
                    fetchMissingAccountConfig(
                        isProductionEnvironment,
                        routerUrl,
                        tribeServerHost,
                        defaultTribe
                    )
                } else {
                    serversUrls.storeRouterUrl(routerUrl)
                    serversUrls.storeTribeServerIp(tribeServerHost)
                }

                delay(100L)

                if (isRestoreAccount) {
                    startRestoreProcess()
                }

                connectionManagerState.value = OwnerRegistrationState.OwnerRegistered
            }
        }
    }

    private fun fetchMissingAccountConfig(
        isProductionEnvironment: Boolean,
        routerUrl: String?,
        tribeServerHost: String?,
        defaultTribe: String?
    ) {
        applicationScope.launch(mainImmediate) {
            networkQueryContact.getAccountConfig(isProductionEnvironment).collect { loadResponse ->
                when (loadResponse) {
                    is Response.Success -> {
                        loadResponse.value.router.takeIf { it.isNotEmpty() && routerUrl.isNullOrEmpty() }?.let {
                            serversUrls.storeRouterUrl(it)
                        }
                        loadResponse.value.tribe_host.takeIf { it.isNotEmpty() && tribeServerHost.isNullOrEmpty() }
                            ?.let {
                                serversUrls.storeTribeServerIp(it)
                            }
                        loadResponse.value.tribe.takeIf { it.isNotEmpty() && defaultTribe.isNullOrEmpty() }?.let {
                            serversUrls.storeDefaultTribe(it)
                        }
                        delay(100L)
                    }

                    is Response.Error -> {
                        // Handle the error, e.g., show a notification or log the error
                        // Navigate to the next screen or perform the next action
                    }
                    LoadResponse.Loading -> {}
                }
            }
        }
    }

    override fun onRestoreAccount(isProductionEnvironment: Boolean) {
        applicationScope.launch(mainImmediate) {
            networkQueryContact.getAccountConfig(isProductionEnvironment).collect { loadResponse ->
                when (loadResponse) {
                    is Response.Success -> {
                        connectManager.restoreAccount(
                            loadResponse.value.tribe,
                            loadResponse.value.tribe_host,
                            loadResponse.value.default_lsp,
                            loadResponse.value.router
                        )
                    }
                    LoadResponse.Loading -> {}
                    is Response.Error -> {}
                }
            }
        }
    }

    override fun onUpsertContacts(
        contacts: List<Pair<String?, Long?>>,
        callback: (() -> Unit)?
    ) {
        if (contacts.isEmpty()) {
            callback?.let { nnCallback ->
                nnCallback()
            }
            return
        }
        applicationScope.launch(mainImmediate) {
            val contactList: List<Pair<MsgSender?, DateTime?>> = contacts.mapNotNull { contact ->
                Pair(contact?.first?.toMsgSenderNull(), contact.second?.toDateTime())
            }.groupBy { it.first?.pubkey }
                .map { (_, group) ->
                    group.find { it.first?.confirmed == true } ?: group.first()
                }

            val newContactList = contactList.map { contactInfo ->
                NewContact(
                    contactAlias = contactInfo.first?.alias?.toContactAlias(),
                    lightningNodePubKey = contactInfo.first?.pubkey?.toLightningNodePubKey(),
                    lightningRouteHint = contactInfo.first?.route_hint?.toLightningRouteHint(),
                    photoUrl = contactInfo.first?.photo_url?.toPhotoUrl(),
                    confirmed = contactInfo.first?.confirmed == true,
                    null,
                    inviteCode = contactInfo.first?.code,
                    invitePrice = null,
                    null,
                    contactInfo.second
                )
            }

            newContactList.forEach { newContact ->
                delay(100L)
                if (newContact.inviteCode != null) {
                    updateNewContactInvited(newContact)
                } else {
                    createNewContact(newContact)
                }
            }

            callback?.let { nnCallback ->
                nnCallback()
            }
        }
    }

    override fun onRestoreMessages() {
        restoreProcessState.value = RestoreProcessState.RestoreMessages
    }

    override fun onUpsertTribes(
        tribes: List<Pair<String?, Boolean?>>,
        isProductionEnvironment: Boolean,
        callback: (() -> Unit)?
    )  {
        if (tribes.isEmpty()) {
            callback?.let { nnCallback ->
                nnCallback()
            }
            return
        }

        applicationScope.launch(io) {
            val total = tribes.count();
            var index = 0

            val tribeList = tribes.mapNotNull { tribes ->
                try {
                    Pair(
                        tribes.first?.toMsgSender(),
                        tribes.second
                    )
                } catch (e: Exception) {
                    null
                }
            }

            tribeList.forEach { tribe ->
                val isAdmin = (tribe.first?.role == 0 && tribe.second == true)
                tribe.first?.let {
                    joinTribeOnRestoreAccount(it, isAdmin, isProductionEnvironment) {
                        if (index == total - 1) {
                            callback?.let { nnCallback ->
                                nnCallback()
                            }
                        } else {
                            index += 1
                        }
                    }
                } ?: run {
                    if (index == total - 1) {
                        callback?.let { nnCallback ->
                            nnCallback()
                        }
                    } else {
                        index += 1
                    }
                }
            }
        }
    }

    override fun onNewBalance(balance: Long) {
        applicationScope.launch(io) {

            balanceLock.withLock {
                accountBalanceStateFlow.value = balance.toNodeBalance()
                networkRefreshBalance.value = balance

                authenticationStorage.putString(
                    REPOSITORY_LIGHTNING_BALANCE,
                    balance.toString()
                )
            }
        }
    }

    override fun onSignedChallenge(sign: String) {
        connectionManagerState.value = OwnerRegistrationState.SignedChallenge(sign)
    }

    override fun onInitialTribe(tribe: String, isProductionEnvironment: Boolean) {
        applicationScope.launch(io) {
            val (host, tribePubKey) = extractUrlParts(tribe)

            if (host == null || tribePubKey == null) {
                return@launch
            }

            networkQueryChat.getTribeInfo(ChatHost(host), LightningNodePubKey(tribePubKey), isProductionEnvironment)
                .collect { loadResponse ->
                    when (loadResponse) {
                        is LoadResponse.Loading -> {}
                        is Response.Error -> {}
                        is Response.Success -> {
                            val queries = coreDB.getSphinxDatabaseQueries()

                            connectManager.joinToTribe(
                                host,
                                tribePubKey,
                                loadResponse.value.route_hint,
                                loadResponse.value.private ?: false,
                                accountOwner.value?.alias?.value ?: "unknown",
                                loadResponse.value.getPriceToJoinInSats()
                            )

                            // TribeId is set from LONG.MAX_VALUE and decremented by 1 for each new tribe
                            val tribeId = queries.chatGetLastTribeId().executeAsOneOrNull()
                                ?.let { it.MIN?.minus(1) }
                                ?: (Long.MAX_VALUE)

                            val now: String = DateTime.nowUTC()

                            val newTribe = Chat(
                                id = ChatId(tribeId),
                                uuid = ChatUUID(tribePubKey),
                                name = ChatName(loadResponse.value.name ?: "unknown"),
                                photoUrl = loadResponse.value.img?.toPhotoUrl(),
                                type = ChatType.Tribe,
                                status = ChatStatus.Approved,
                                contactIds = listOf(ContactId(0), ContactId(tribeId)),
                                isMuted = ChatMuted.False,
                                createdAt = now.toDateTime(),
                                groupKey = null,
                                host = ChatHost(host),
                                pricePerMessage = loadResponse.value.getPricePerMessageInSats().toSat(),
                                escrowAmount = loadResponse.value.getEscrowAmountInSats().toSat(),
                                unlisted = ChatUnlisted.False,
                                privateTribe = ChatPrivate.False,
                                ownerPubKey = LightningNodePubKey(tribePubKey),
                                seen = Seen.False,
                                metaData = null,
                                myPhotoUrl = null,
                                myAlias = null,
                                pendingContactIds = emptyList(),
                                latestMessageId = null,
                                contentSeenAt = null,
                                notify = NotificationLevel.SeeAll,
                                secondBrainUrl = null
                            )

                            chatLock.withLock {
                                queries.transaction {
                                    upsertNewChat(
                                        newTribe,
                                        SynchronizedMap<ChatId, Seen>(),
                                        queries,
                                        null,
                                        accountOwner.value?.nodePubKey
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    private suspend fun joinTribeOnRestoreAccount(
        contactInfo: MsgSender,
        isAdmin: Boolean,
        isProductionEnvironment: Boolean,
        callback: (() -> Unit)? = null
    ) {
        val host = contactInfo.host ?: return

        withContext(dispatchers.io) {
            networkQueryChat.getTribeInfo(ChatHost(host), LightningNodePubKey(contactInfo.pubkey), isProductionEnvironment)
                .collect { loadResponse ->
                    when (loadResponse) {
                        is LoadResponse.Loading -> {}
                        is Response.Error -> {
                            callback?.let {nnCallback ->
                                nnCallback()
                            }
                        }
                        is Response.Success -> {
                            val queries = coreDB.getSphinxDatabaseQueries()

                            // TribeId is set from LONG.MAX_VALUE and decremented by 1 for each new tribe
                            val tribeId = queries.chatGetLastTribeId().executeAsOneOrNull()
                                ?.let { it.MIN?.minus(1) }
                                ?: (Long.MAX_VALUE)

                            val now: String = DateTime.nowUTC()

                            val newTribe = Chat(
                                id = ChatId(tribeId),
                                uuid = ChatUUID(contactInfo.pubkey),
                                name = ChatName(loadResponse.value.name),
                                photoUrl = loadResponse.value.img?.toPhotoUrl(),
                                type = ChatType.Tribe,
                                status = ChatStatus.Approved,
                                contactIds = listOf(ContactId(0), ContactId(tribeId)),
                                isMuted = ChatMuted.False,
                                createdAt = now.toDateTime(),
                                groupKey = null,
                                host = contactInfo.host?.toChatHost(),
                                pricePerMessage = loadResponse.value.getPricePerMessageInSats().toSat(),
                                escrowAmount = loadResponse.value.getEscrowAmountInSats().toSat(),
                                unlisted = loadResponse.value.unlisted?.toChatUnlisted() ?: ChatUnlisted.False,
                                privateTribe = loadResponse.value.private.toChatPrivate(),
                                ownerPubKey = if (isAdmin) accountOwner.value?.nodePubKey else LightningNodePubKey(contactInfo.pubkey),
                                seen = Seen.False,
                                metaData = null,
                                myPhotoUrl = null,
                                myAlias = null,
                                pendingContactIds = emptyList(),
                                latestMessageId = null,
                                contentSeenAt = null,
                                notify = NotificationLevel.SeeAll,
                                secondBrainUrl = loadResponse.value.second_brain_url?.toSecondBrainUrl()
                            )

                            messageLock.withLock {
                                chatLock.withLock {
                                    queries.transaction {
                                        upsertNewChat(
                                            newTribe,
                                            SynchronizedMap<ChatId, Seen>(),
                                            queries,
                                            null,
                                            accountOwner.value?.nodePubKey
                                        )
                                    }
                                }
                            }

                            callback?.let {nnCallback ->
                                nnCallback()
                            }
                        }
                    }
                }
        }
    }


    override fun onLastReadMessages(lastReadMessages: String) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val lastReadMessagesMap = lastReadMessages.toLastReadMap()
            val pubKeys = lastReadMessagesMap?.keys

            val contactPubkey = pubKeys?.map { it.toLightningNodePubKey() }
            val tribePubKey = pubKeys?.map { it.toChatUUID() }

            val contacts = contactPubkey?.filterNotNull()?.let { queries.contactGetAllByPubKeys(it).executeAsList() }
            val tribes = tribePubKey?.filterNotNull()?.let { queries.chatGetAllByUUIDS(it).executeAsList() }

            // Create a new map for mapping chatId to lastMsgIndex
            val chatIdToLastMsgIndexMap = mutableMapOf<ChatId, MessageId>()

            contacts?.forEach { contact ->
                val lastMsgIndex = lastReadMessagesMap.get(contact.node_pub_key?.value)
                if (lastMsgIndex != null) {
                    chatIdToLastMsgIndexMap[ChatId(contact.id.value)] = MessageId(lastMsgIndex)
                }
            }

            tribes?.forEach { tribe ->
                val lastMsgIndex = lastReadMessagesMap.get(tribe.uuid.value)
                if (lastMsgIndex != null) {
                    chatIdToLastMsgIndexMap[tribe.id] = MessageId(lastMsgIndex)
                }
            }

            messageLock.withLock {
                queries.transaction {
                    chatIdToLastMsgIndexMap.forEach { (chatId, lastMsgIndex) ->
                        queries.messageUpdateSeenByChatIdAndId(chatId, lastMsgIndex)
                    }
                }
            }

            chatLock.withLock {
                chatIdToLastMsgIndexMap.forEach { (chatId, lastMsgIndex) ->
                    queries.chatUpdateSeenByLastMessage(chatId, lastMsgIndex)
                }
            }
        }
    }

    override fun onUpdateMutes(mutes: String) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val mutesMap = mutes.toMuteLevelsMap()
            val pubKeys = mutesMap?.keys

            val contactPubkey = pubKeys?.map { it.toLightningNodePubKey() }
            val tribePubKey = pubKeys?.map { it.toChatUUID() }

            val contacts = contactPubkey?.filterNotNull()?.let { queries.contactGetAllByPubKeys(it).executeAsList() }
            val tribes = tribePubKey?.filterNotNull()?.let { queries.chatGetAllByUUIDS(it).executeAsList() }

            val notificationMap = mutableMapOf<ChatId, NotificationLevel>()

            contacts?.forEach { contact ->
                mutesMap[contact.node_pub_key?.value]?.let { level ->
                    notificationMap[ChatId(contact.id.value)] = when (level) {
                        NotificationLevel.SEE_ALL -> NotificationLevel.SeeAll
                        NotificationLevel.ONLY_MENTIONS -> NotificationLevel.OnlyMentions
                        NotificationLevel.MUTE_CHAT -> NotificationLevel.MuteChat
                        else -> NotificationLevel.Unknown(level)
                    }
                }
            }

            tribes?.forEach { tribe ->
                mutesMap[tribe.uuid.value]?.let { level ->
                    notificationMap[ChatId(tribe.id.value)] = when (level) {
                        NotificationLevel.SEE_ALL -> NotificationLevel.SeeAll
                        NotificationLevel.ONLY_MENTIONS -> NotificationLevel.OnlyMentions
                        NotificationLevel.MUTE_CHAT -> NotificationLevel.MuteChat
                        else -> NotificationLevel.Unknown(level)
                    }
                }
            }

            chatLock.withLock {
                queries.transaction {
                    notificationMap.forEach { (chatId, level) ->
                        queries.chatUpdateNotificationLevel(level, chatId)
                    }
                }
            }
        }
    }

    override fun onGetNodes() {
//        connectionManagerState.value = OwnerRegistrationState.GetNodes
    }

    override fun listenToOwnerCreation(callback: () -> Unit) {
        applicationScope.launch(mainImmediate) {
            accountOwner.filter { contact ->
                contact != null && !contact.routeHint?.value.isNullOrEmpty()
            }
                .map { true }
                .first()

            withContext(dispatchers.mainImmediate) {
                delay(1000L)
                callback.invoke()
            }
        }
    }

    override fun onConnectManagerError(error: ConnectManagerError) {
        connectManagerErrorState.value = error
    }

    override fun onRestoreProgress(progress: Int) {
        if (progress < 100) {
            restoreProgress.value = RestoreProgress(true, progress)
        }
    }

    override fun onRestoreFinished(isRestoreCancelled: Boolean) {
        restoreProgress.value = if (isRestoreCancelled) null else RestoreProgress(false, 100)
        applicationScope.launch(io) {
            val messageId = restoreMinIndex.value?.let { MessageId(it) }

            if (messageId != null) {
                val message = getMessageById(messageId).firstOrNull()
                if (message != null) {
                    connectManager.getReadMessages()
                    restoreMinIndex.value = null
                }
            }
            val owner = getOwner()
            if (owner?.alias?.value?.trim().isNullOrEmpty()) {
                profileSetInfoRestore.value = true
            }
        }
    }

    override fun updatePaidInvoices() {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            queries.messageGetAllPayments().executeAsList().forEach { message ->
                messageLock.withLock {
                    queries.messageUpdateInvoiceAsPaidByPaymentHash(message.payment_hash)
                }
            }
        }
    }

    // Messaging Callbacks
    override fun onMessage(
        msg: String,
        msgSender: String,
        msgType: Int,
        msgUuid: String,
        msgIndex: String,
        msgTimestamp: Long?,
        sentTo: String,
        amount: Long?,
        fromMe: Boolean?,
        tag: String?,
        date: Long?,
        isRestore: Boolean
    ) {
        applicationScope.launch(io) {
            try {
                val messageType = msgType.toMessageType()
                val messageSender = if (msgSender.isNotEmpty()) msgSender.toMsgSender() else null

                if (messageSender != null ) {

                    val contactTribePubKey = if (fromMe == true) {
                        sentTo
                    } else {
                        messageSender.pubkey
                    }

                    when (messageType) {
                        is MessageType.ContactKeyRecord -> {
                            saveNewContactRegistered(msgSender, date, isRestore)
                        }
                        else -> {
                            val message = if (msg.isNotEmpty()) msg.toMsg() else Msg(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                            )

                            when (messageType) {
                                is MessageType.Purchase.Processing -> {
                                    amount?.toSat()?.let { paidAmount ->
                                        sendMediaKeyOnPaidPurchase(
                                            message,
                                            messageSender,
                                            paidAmount
                                        )
                                    }
                                }

                                is MessageType.ContactKeyConfirmation -> {
                                    saveNewContactRegistered(msgSender, date, isRestore)
                                }

                                is MessageType.ContactKey -> {
                                    saveNewContactRegistered(msgSender, date, isRestore)
                                }

                                is MessageType.Delete -> {
                                    msg.toMsg().replyUuid?.toMessageUUID()?.let { replyUuid ->
                                        deleteMqttMessage(replyUuid)
                                    }
                                }

                                else -> {}
                            }

                            val messageId = if (msgIndex.isNotEmpty()) MessageId(msgIndex.toLong()) else return@launch
                            val messageUuid = msgUuid.toMessageUUID() ?: return@launch
                            val originalUUID = message.originalUuid?.toMessageUUID()
                            val timestamp = msgTimestamp?.toDateTime()
                            val date = message.date?.toDateTime()
                            val paymentRequest = message.invoice?.toLightningPaymentRequestOrNull()
                            val bolt11 = paymentRequest?.let { Bolt11.decode(it) }
                            val paymentHash = paymentRequest?.let {
                                connectManager.retrievePaymentHash(it.value)?.toLightningPaymentHash()
                            }
                            val msgTag = tag?.toTagMessage()

                            upsertMqttMessage(
                                message,
                                messageSender,
                                contactTribePubKey,
                                messageType,
                                messageUuid,
                                messageId,
                                message.amount?.milliSatsToSats(),
                                originalUUID,
                                timestamp,
                                date,
                                fromMe ?: false,
                                amount?.toSat(),
                                paymentRequest,
                                paymentHash,
                                bolt11,
                                msgTag
                            )
                        }
                    }
                } else {
                    val message = if (msg.isNotEmpty()) msg.toMsg() else Msg(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )

                    if (msgIndex.isNotEmpty() && message.content?.isNotEmpty() == true) {
                        val messageId = MessageId(msgIndex.toLong())

                        upsertGenericPaymentMsg(
                            msg = message,
                            msgType = msgType.toMessageType(),
                            msgIndex = messageId,
                            msgAmount = amount?.toSat(),
                            timestamp = msgTimestamp?.toDateTime(),
                            paymentHash = message.paymentHash?.toLightningPaymentHash()
                        )
                    }
                }
            } catch (e: Exception) {
                LOG.e(TAG, "onMessage: ${e.message}", e)
            }
        }
    }

    override fun onMessageTagAndUuid(tag: String?, msgUUID: String, provisionalId: Long) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val tagMessage = tag?.let { TagMessage(it) }

            // messageUpdateTagAndUUID also updates the Status to CONFIRMED
            messageLock.withLock {
                queries.messageUpdateTagAndUUID(tagMessage, MessageUUID(msgUUID), MessageId(provisionalId))
            }
        }
    }

    override fun onMessagesCounts(msgsCounts: String) {
        try {
            msgsCounts.toMsgsCounts()?.let {
                restoreProcessState.value = RestoreProcessState.MessagesCounts(it)
                connectManager.saveMessagesCounts(it)
            }
        } catch (e: Exception) {
            LOG.e(TAG, "onMessagesCounts: ${e.message}", e)
        }
    }

    override fun onSentStatus(sentStatus: String) {
        applicationScope.launch(io) {
            val newSentStatus = sentStatus.toNewSentStatus()
            val queries = coreDB.getSphinxDatabaseQueries()

            if (newSentStatus.tag == processingInvoice.value?.second) {
                if (newSentStatus.isFailedMessage()) {
                    processingInvoice.value?.first?.toLightningPaymentRequestOrNull()?.let {
                        payInvoiceFromLSP(it)
                    }
                }
                processingInvoice.value = null
            } else {
                if (newSentStatus.isFailedMessage()) {
                    queries.messageUpdateStatusAndPaymentHashByTag(
                        MessageStatus.Failed,
                        newSentStatus.payment_hash?.toLightningPaymentHash(),
                        newSentStatus.message?.toErrorMessage(),
                        newSentStatus.tag?.toTagMessage()
                    )
                } else {
                    queries.messageUpdateStatusAndPaymentHashByTag(
                        MessageStatus.Received,
                        newSentStatus.payment_hash?.toLightningPaymentHash(),
                        newSentStatus.message?.toErrorMessage(),
                        newSentStatus.tag?.toTagMessage()
                    )

                    // Check if web view payment hash matches
                    if (newSentStatus.payment_hash == webViewPaymentHash.value) {
                        webViewPreImage.value = newSentStatus.preimage
                        webViewPaymentHash.value = null
                    }
                }
            }
        }
    }

    override fun onMessageTagList(tags: String) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val tagsList = tags.toTagsList()

            queries.transaction {
                tagsList?.forEach { tag ->
                    tag.status?.toMessageStatus()?.let { messageStatus ->
                        queries.messageUpdateStatusByTag(
                            messageStatus,
                            tag.error?.toErrorMessage(),
                            tag.tag?.toTagMessage()
                        )
                    }
                }
            }
        }
    }

    override fun onRestoreMinIndex(minIndex: Long) {
        restoreMinIndex.value = minIndex
    }

    // Tribe Management Callbacks
    override fun onNewTribeCreated(newTribe: String) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val newCreateTribe = newTribe.toNewCreateTribe()
            newCreateTribe.pubkey?.let { tribePubKey ->

                val existingTribe = tribePubKey.toChatUUID()?.let { getChatByUUID(it) }?.firstOrNull()
                // TribeId is set from LONG.MAX_VALUE and decremented by 1 for each new tribe
                val tribeId = existingTribe?.id?.value ?: queries.chatGetLastTribeId().executeAsOneOrNull()?.let { it.MIN?.minus(1) }
                ?: (Long.MAX_VALUE)
                val now: String = DateTime.nowUTC()

                val chatTribe = Chat(
                    id = ChatId(tribeId),
                    uuid = ChatUUID(tribePubKey),
                    name = ChatName(newCreateTribe.name),
                    photoUrl = newCreateTribe.img?.toPhotoUrl(),
                    type = ChatType.Tribe,
                    status = ChatStatus.Approved,
                    contactIds = listOf(ContactId(0), ContactId(tribeId)),
                    isMuted = ChatMuted.False,
                    createdAt = newCreateTribe.created?.toDateTime() ?: now.toDateTime(),
                    groupKey = existingTribe?.groupKey,
                    host = existingTribe?.host,
                    pricePerMessage = newCreateTribe.getPricePerMessageInSats().toSat(),
                    escrowAmount = newCreateTribe.getEscrowAmountInSats().toSat(),
                    unlisted = if (newCreateTribe.unlisted == true) ChatUnlisted.True else ChatUnlisted.False,
                    privateTribe = if (newCreateTribe.private == true) ChatPrivate.True else ChatPrivate.False,
                    ownerPubKey = accountOwner.value?.nodePubKey,
                    seen = Seen.False,
                    metaData = existingTribe?.metaData,
                    myPhotoUrl = accountOwner.value?.photoUrl,
                    myAlias = ChatAlias(newCreateTribe.owner_alias),
                    pendingContactIds = emptyList(),
                    latestMessageId = existingTribe?.latestMessageId,
                    contentSeenAt = existingTribe?.contentSeenAt,
                    notify = NotificationLevel.SeeAll,
                    secondBrainUrl = existingTribe?.secondBrainUrl
                )

                chatLock.withLock {
                    queries.transaction {
                        upsertNewChat(
                            chatTribe,
                            SynchronizedMap<ChatId, Seen>(),
                            queries,
                            null,
                            accountOwner.value?.nodePubKey
                        )
                    }
                }
            }
        }
    }

    override fun onTribeMembersList(tribeMembers: String) {
        applicationScope.launch(mainImmediate) {
            try {
                tribeMembers.toTribeMembersList()?.let { members ->
                    tribeMembersState.value = members
                }
            } catch (e: Exception) {

            }
        }
    }

    // Invoice and Payment Management Callbacks

    override fun onPayments(payments: String) {
        applicationScope.launch(io) {
            val paymentsJson = payments.toPaymentsList()

            val paymentsReceived = paymentsJson?.mapNotNull {
                it.msg_idx?.let { msgId ->
                    MessageId(msgId)
                }
            }

            val paymentsSent = paymentsJson?.mapNotNull {
                it.rhash?.let { hash ->
                    LightningPaymentHash(hash)
                }
            }

            val paymentsReceivedMsgs = paymentsReceived?.let {
                getMessagesByIds(it).firstOrNull()
            }

            val paymentsSentMsgs = paymentsSent?.let {
                getMessagesByPaymentHashes(it).firstOrNull()
            }

            // Combine all retrieved messages from DB
            val combinedMessages: List<Message?> = paymentsReceivedMsgs.orEmpty() + paymentsSentMsgs.orEmpty()

            // Generate TransactionDto from the combinedMessages list or from the raw payments data
            val transactionDtoList = paymentsJson?.map { payment ->
                // Try to find corresponding DB message first
                val dbMessage = combinedMessages.firstOrNull {
                    it?.id?.value == payment.msg_idx || it?.paymentHash?.value == payment.rhash
                }

                dbMessage?.takeIf { it.type !is MessageType.Invoice }?.let { message ->
                    // If found in DB, build TransactionDto using DB information
                    TransactionDto(
                        id = message.id.value,
                        chat_id = message.chatId.value,
                        type = message.type.value,
                        sender = message.sender.value,
                        sender_alias = message.senderAlias?.value,
                        receiver = message.receiver?.value,
                        amount = message.amount.value,
                        payment_hash = message.paymentHash?.value,
                        payment_request = message.paymentRequest?.value,
                        date = message.date.time,
                        reply_uuid = message.replyUUID?.value,
                        error_message = message.errorMessage?.value
                    )
                } ?: run {
                    // If not found in DB, create TransactionDto with available information from the Payment object
                    TransactionDto(
                        id = payment.msg_idx ?: 0L,
                        chat_id = null,
                        type = MessageType.DirectPayment.value,
                        sender = 0L,
                        sender_alias = null,
                        receiver = null,
                        amount = payment.amt_msat?.milliSatsToSats()?.value ?: 0L,
                        payment_hash = payment.rhash,
                        payment_request = null,
                        date = payment.ts,
                        reply_uuid = null,
                        error_message = payment.error
                    )
                }
            }.orEmpty()

            // Sort the transactions by date and set the result to the state
            transactionDtoState.value = transactionDtoList.sortedByDescending { it.date}.distinct()
        }
    }

    override fun onNetworkStatusChange(
        isConnected: Boolean,
        isLoading: Boolean
    ) {
        if (isConnected) {
            networkStatus.value = NetworkStatus.Connected
        } else if (isLoading) {
            networkStatus.value = NetworkStatus.Loading
        } else {
            networkStatus.value = NetworkStatus.Disconnected
            reconnectMqtt()
        }
    }
    override fun onNewInviteCreated(
        nickname: String,
        inviteString: String,
        inviteCode: String,
        sats: Long
    ) {
        applicationScope.launch(mainImmediate) {
            val newInvitee = NewContact(
                contactAlias = nickname.toContactAlias(),
                lightningNodePubKey = null,
                lightningRouteHint = null,
                photoUrl = null,
                confirmed = false,
                inviteString = inviteString,
                inviteCode = inviteCode,
                invitePrice = sats.toSat(),
                inviteStatus = InviteStatus.Pending,
                null
            )
            createNewContact(newInvitee)
            println("CREATE_CONTACT: onNewInviteCreated")
        }
    }

    override fun onPerformDelay(delay: Long, callback: () -> Unit) {
        applicationScope.launch(mainImmediate) {
            delay(delay)
            callback.invoke()
        }
    }

    fun extractUrlParts(url: String): Pair<String?, String?> {
        val cleanUrl = url.replace(Regex("^[a-zA-Z]+://"), "")
        val separatorIndex = cleanUrl.indexOf("/")

        if (separatorIndex == -1) return null to null
        val host = cleanUrl.substring(0, separatorIndex).takeIf { it.isNotEmpty() }
        val tribePubKey = cleanUrl.substring(separatorIndex + 1).split("/").lastOrNull()?.takeIf { it.isNotEmpty() }

        return host to tribePubKey
    }


    /**
     * V1 code below this comment
     * */

    override var updatedContactIds: MutableList<ContactId> = mutableListOf()


    private fun showNotification(
        chatId: ChatId,
        chatDto: ChatDto?,
        messageDto: MessageDto,
        contactDto: ContactDto?,
    ) {
        applicationScope.launch(mainImmediate) {
            if (chatDto?.isMutedActual() == true) {
                return@launch
            }

            if (chatDto?.isOnlyMentions() == true) {
                val alias = chatDto?.my_alias ?: getOwner()?.alias?.value ?: ""

                if ((messageDto.messageContentDecrypted?.lowercase()?.contains("@${alias.lowercase()}") == false)) {
                    return@launch
                }
            }

            if (chatDto?.is_muted?.value == false) {
                val sender = messageDto.sender_alias ?: contactDto?.alias
                val chatName = chatDto?.name

                sender?.let { senderAlias ->
                    sphinxNotificationManager.notify(
                        notificationId = chatId.value,
                        title = if (chatName != null) {
                            "$chatName: message from $senderAlias"
                        } else {
                            "Message from $senderAlias"
                        },
                        message = messageDto.getNotificationText() ?: ""
                    )
                }
            }
        }
    }

    /////////////
    /// Chats ///
    /////////////
    private val chatLock = Mutex()
    private val chatDboPresenterMapper: ChatDboPresenterMapper by lazy {
        ChatDboPresenterMapper(dispatchers)
    }

    override suspend fun getAllChats(): List<Chat> = coreDB.getSphinxDatabaseQueries().chatGetAll()
        .executeAsList()
        .map {
            chatDboPresenterMapper.mapFrom(it)
        }

    override val getAllChatsFlow: Flow<List<Chat>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().chatGetAll()
                    .asFlow()
                    .mapToList(io)
                    .map { chatDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override val getAllContactChatsFlow: Flow<List<Chat>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().chatGetAllContact()
                    .asFlow()
                    .mapToList(io)
                    .map { chatDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override val getAllTribeChatsFlow: Flow<List<Chat>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().chatGetAllTribe()
                    .asFlow()
                    .mapToList(io)
                    .map { chatDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override suspend fun getAllChatsByIds(chatIds: List<ChatId>): List<Chat> {
        return coreDB.getSphinxDatabaseQueries()
            .chatGetAllByIds(chatIds)
            .executeAsList()
            .map { chatDboPresenterMapper.mapFrom(it) }
    }

    override suspend fun getChatById(chatId: ChatId): Chat? {
        val chatDbo = coreDB.getSphinxDatabaseQueries().chatGetById(chatId)
            .executeAsOneOrNull()
        return chatDbo?.let { chatDboPresenterMapper.mapFrom(it) }
    }

    override fun getChatByIdFlow(chatId: ChatId): Flow<Chat?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().chatGetById(chatId)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { chatDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override fun getChatByUUID(chatUUID: ChatUUID): Flow<Chat?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().chatGetByUUID(chatUUID)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { chatDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override fun getConversationByContactIdFlow(contactId: ContactId): Flow<Chat?> = flow {
        var ownerId: ContactId? = accountOwner.value?.id

        if (ownerId == null) {
            try {
                accountOwner.collect {
                    if (it != null) {
                        ownerId = it.id
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}
            delay(25L)
        }

        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .chatGetConversationForContact(
                    if (ownerId != null) {
                        listOf(ownerId!!, contactId)
                    } else {
                        listOf()
                    }
                )
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { chatDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override fun getUnseenMessagesByChatId(chatId: ChatId): Flow<Long?> = flow {
        var ownerId: ContactId? = accountOwner.value?.id

        if (ownerId == null) {
            try {
                accountOwner.collect { contact ->
                    if (contact != null) {
                        ownerId = contact.id
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}
            delay(25L)
        }

        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetUnseenIncomingMessageCountByChatId(
                    ownerId ?: ContactId(-1),
                    chatId
                )
                .asFlow()
                .mapToOneOrNull(io)
                .distinctUntilChanged()
        )
    }

    override fun getUnseenMentionsByChatId(chatId: ChatId): Flow<Long?> = flow {
        var ownerId: ContactId? = accountOwner.value?.id

        if (ownerId == null) {
            try {
                accountOwner.collect { contact ->
                    if (contact != null) {
                        ownerId = contact.id
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}
            delay(25L)
        }

        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetUnseenIncomingMentionsCountByChatId(
                    ownerId ?: ContactId(-1),
                    chatId
                )
                .asFlow()
                .mapToOneOrNull(io)
                .distinctUntilChanged()
        )
    }

    override fun getUnseenActiveConversationMessagesCount(): Flow<Long?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        var ownerId: ContactId? = accountOwner.value?.id

        if (ownerId == null) {
            try {
                accountOwner.collect { contact ->
                    if (contact != null) {
                        ownerId = contact.id
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}
            delay(25L)
        }

        val blockedContactIds = queries.contactGetBlocked().executeAsList().map { it.id }

        emitAll(
            queries
                .messageGetUnseenIncomingMessageCountByChatType(
                    ownerId ?: ContactId(-1),
                    blockedContactIds,
                    ChatType.Conversation
                )
                .asFlow()
                .mapToOneOrNull(io)
                .distinctUntilChanged()
        )
    }

    override fun getUnseenTribeMessagesCount(): Flow<Long?> = flow {
        var ownerId: ContactId? = accountOwner.value?.id

        if (ownerId == null) {
            try {
                accountOwner.collect { contact ->
                    if (contact != null) {
                        ownerId = contact.id
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}
            delay(25L)
        }

        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetUnseenIncomingMessageCountByChatType(
                    ownerId ?: ContactId(-1),
                    listOf(),
                    ChatType.Tribe
                )
                .asFlow()
                .mapToOneOrNull(io)
                .distinctUntilChanged()
        )
    }

    override fun getPaymentsTotalFor(feedId: FeedId): Flow<Sat?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetAmountSumForMessagesStartingWith(
                    "{\"feedID\":${feedId.value.toLongOrNull()}%",
                    "{\"feedID\":\"${feedId.value}\"%"
                )
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.SUM }
                .distinctUntilChanged()
        )
    }

    override fun getSentConfirmedMessagesByChatId(chatId: ChatId): Flow<List<Message>> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetSentConfirmedMessages(chatId)
                .asFlow()
                .mapToList(io)
                .map { listMessageDbo ->
                    listMessageDbo.map {
                        messageDboPresenterMapper.mapFrom(it)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override val networkRefreshChatsFlow: Flow<LoadResponse<Boolean, ResponseError>> by lazy {
        flow {
            // TODO V2 getChats
//            networkQueryChat.getChats().collect { loadResponse ->
//
//                Exhaustive@
//                when (loadResponse) {
//                    is Response.Error -> {
//                        emit(loadResponse)
//                    }
//                    is Response.Success -> {
//                        emit(processChatDtos(loadResponse.value))
//                    }
//                    is LoadResponse.Loading -> {
//                        emit(loadResponse)
//                    }
//                }
//
//            }
        }
    }

    override fun updateChatMetaData(
        chatId: ChatId,
        podcastId: FeedId?,
        metaData: ChatMetaData,
        shouldSync: Boolean
    ) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            if (chatId.value == ChatId.NULL_CHAT_ID.toLong()) {
                //Podcast with no chat. Updating current item id
                podcastId?.let { nnPodcastId ->
                    podcastLock.withLock {
                        queries.feedUpdateCurrentItemId(
                            metaData.itemId,
                            nnPodcastId
                        )
                    }
                }
                return@launch
            }

            chatLock.withLock {
                queries.chatUpdateMetaData(metaData, chatId)
            }

            podcastLock.withLock {
                queries.feedUpdateCurrentItemIdByChatId(
                    metaData.itemId,
                    chatId
                )
            }

            if (shouldSync) {
                try {
                    // TODO V2 updateChat
                } catch (e: AssertionError) {
                }
            }
        }
    }

    override fun streamFeedPayments(
        chatId: ChatId,
        metaData: ChatMetaData,
        podcastId: String,
        episodeId: String,
        destinations: List<FeedDestination>,
        updateMetaData: Boolean,
        clipMessageUUID: MessageUUID?,
    ) {

        if (chatId.value == ChatId.NULL_CHAT_ID.toLong()) {
            return
        }

        if (metaData.satsPerMinute.value <= 0 || destinations.isEmpty()) {
            return
        }

        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            chatLock.withLock {
                queries.chatUpdateMetaData(metaData, chatId)
            }

            val destinationsArray: MutableList<PostStreamSatsDestinationDto> =
                ArrayList(destinations.size)

            for (destination in destinations) {
                destinationsArray.add(
                    PostStreamSatsDestinationDto(
                        destination.address.value,
                        destination.type.value,
                        destination.split.value,
                    )
                )
            }

            val streamSatsText =
                StreamSatsText(podcastId, episodeId, metaData.timeSeconds.toLong(), metaData.speed, clipMessageUUID?.value)

            val postStreamSatsDto = PostStreamSatsDto(
                metaData.satsPerMinute.value,
                chatId.value,
                streamSatsText.toJson(),
                updateMetaData,
                destinationsArray
            )

            try {
                // TODO V2 stremSats
//                networkQueryChat.streamSats(
//                    postStreamSatsDto
//                ).collect {}
            } catch (e: AssertionError) {
            }
        }
    }


    ////////////////
    /// Contacts ///
    ////////////////
    private val contactLock = Mutex()
    private val contactDboPresenterMapper: ContactDboPresenterMapper by lazy {
        ContactDboPresenterMapper(dispatchers)
    }
    private val inviteDboPresenterMapper: InviteDboPresenterMapper by lazy {
        InviteDboPresenterMapper(dispatchers)
    }

    override val getAllContacts: Flow<List<Contact>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().contactGetAll()
                    .asFlow()
                    .mapToList(io)
                    .map { contactDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override val getAllNotBlockedContacts: Flow<List<Contact>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().contactGetNotBlocked()
                    .asFlow()
                    .mapToList(io)
                    .map { contactDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override val getAllInvites: Flow<List<Invite>> by lazy {
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries().inviteGetAll()
                    .asFlow()
                    .mapToList(io)
                    .map { inviteDboPresenterMapper.mapListFrom(it) }
            )
        }
    }

    override fun getContactById(contactId: ContactId): Flow<Contact?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().contactGetById(contactId)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { contactDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override fun getContactByPubKey(pubKey: LightningNodePubKey): Flow<Contact?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().contactGetByPubKey(pubKey)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { contactDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override suspend fun getAllContactsByIds(contactIds: List<ContactId>): List<Contact> {
        return coreDB.getSphinxDatabaseQueries()
            .contactGetAllByIds(contactIds)
            .executeAsList()
            .map { contactDboPresenterMapper.mapFrom(it) }
    }

    override fun getInviteByContactId(contactId: ContactId): Flow<Invite?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().inviteGetByContactId(contactId)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { inviteDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override fun getInviteById(inviteId: InviteId): Flow<Invite?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().inviteGetById(inviteId)
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.let { inviteDboPresenterMapper.mapFrom(it) } }
                .distinctUntilChanged()
        )
    }

    override val networkRefreshContacts: Flow<LoadResponse<Boolean, ResponseError>> by lazy {
        flow {
            // TODO V2 getContacts

//            networkQueryContact.getContacts().collect { loadResponse ->
//
//                Exhaustive@
//                when (loadResponse) {
//                    is Response.Error -> {
//                        emit(loadResponse)
//                    }
//                    is Response.Success -> {
//
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        try {
//                            var error: Throwable? = null
//                            val handler = CoroutineExceptionHandler { _, throwable ->
//                                error = throwable
//                            }
//
//                            var processChatsResponse: Response<Boolean, ResponseError> =
//                                Response.Success(true)
//
//                            applicationScope.launch(io + handler) {
//
//                                val contactMap: MutableMap<ContactId, ContactDto> =
//                                    LinkedHashMap(loadResponse.value.contacts.size)
//
//                                chatLock.withLock {
//                                    messageLock.withLock {
//                                        contactLock.withLock {
//
//                                            val contactIdsToRemove = queries.contactGetAllIds()
//                                                .executeAsList()
//                                                .toMutableSet()
//
//                                            queries.transaction {
//                                                for (dto in loadResponse.value.contacts) {
//
//                                                    upsertContact(dto, queries)
//                                                    contactMap[ContactId(dto.id)] = dto
//
//                                                    contactIdsToRemove.remove(ContactId(dto.id))
//
//                                                }
//
//                                                for (contactId in contactIdsToRemove) {
//                                                    deleteContactById(contactId, queries)
//                                                }
//
//                                            }
//
//                                        }
//                                    }
//
//                                }
//
//                                processChatsResponse = processChatDtos(
//                                    loadResponse.value.chats,
//                                    contactMap,
//                                )
//                            }.join()
//
//                            error?.let {
//                                throw it
//                            }
//
//                            emit(processChatsResponse)
//
//                        } catch (e: ParseException) {
//                            val msg =
//                                "Failed to convert date/time from Relay while processing Contacts"
//                            LOG.e(TAG, msg, e)
//                            emit(Response.Error(ResponseError(msg, e)))
//                        }
//
//                    }
//                    is LoadResponse.Loading -> {
//                        emit(loadResponse)
//                    }
//                }
//            }
        }
    }

    private val inviteLock = Mutex()
    override val networkRefreshLatestContacts: Flow<LoadResponse<RestoreProgress, ResponseError>> by lazy {
        flow {

            val lastSeenContactsDate: String? = authenticationStorage.getString(
                REPOSITORY_LAST_SEEN_CONTACTS_DATE,
                null
            )

            val lastSeenContactsDateResolved: DateTime = lastSeenContactsDate?.toDateTime()
                ?: DATE_NIXON_SHOCK.toDateTime()

            val now: String = DateTime.nowUTC()
            val restoring = lastSeenContactsDate == null

            emit(
                Response.Success(
                    RestoreProgress(restoring, 2)
                )
            )
            // TODO V2 getLatestContacts
//            networkQueryContact.getLatestContacts(
//                lastSeenContactsDateResolved
//            ).collect { loadResponse ->
//
//                Exhaustive@
//                when (loadResponse) {
//                    is Response.Error -> {
//                        emit(loadResponse)
//                    }
//                    is Response.Success -> {
//
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        try {
//                            var error: Throwable? = null
//                            val handler = CoroutineExceptionHandler { _, throwable ->
//                                error = throwable
//                            }
//
//                            var processChatsResponse: Response<Boolean, ResponseError> =
//                                Response.Success(true)
//
//                            applicationScope.launch(io + handler) {
//
//                                val contactsToInsert = loadResponse.value.contacts.filter { dto -> !dto.deletedActual && !dto.fromGroupActual }
//                                val contactMap: MutableMap<ContactId, ContactDto> =
//                                    LinkedHashMap(contactsToInsert.size)
//
//                                contactLock.withLock {
//                                    inviteLock.withLock {
//                                        queries.transaction {
//                                            for (dto in loadResponse.value.contacts) {
//                                                if (dto.deletedActual || dto.fromGroupActual) {
//                                                    deleteContactById(ContactId(dto.id), queries)
//                                                } else {
//                                                    upsertContact(dto, queries)
//                                                    contactMap[ContactId(dto.id)] = dto
//                                                }
//                                            }
//
//                                            for (dto in loadResponse.value.invites) {
//                                                updatedContactIds.add(ContactId(dto.contact_id))
//                                                upsertInvite(dto, queries)
//                                            }
//                                        }
//                                    }
//                                }
//
//                                processChatsResponse = processChatDtos(
//                                    loadResponse.value.chats,
//                                    contactMap,
//                                )
//
//                                subscriptionLock.withLock {
//                                    queries.transaction {
//                                        for (dto in loadResponse.value.subscriptions) {
//                                            upsertSubscription(dto, queries)
//                                        }
//                                    }
//                                }
//
//                            }.join()
//
//                            error?.let {
//                                throw it
//                            } ?: run {
//                                if (
//                                    loadResponse.value.contacts.size > 1 ||
//                                    loadResponse.value.chats.isNotEmpty()
//                                ) {
//                                    authenticationStorage.putString(
//                                        REPOSITORY_LAST_SEEN_CONTACTS_DATE,
//                                        now
//                                    )
//                                }
//                            }
//
//                            emit(
//                                if (processChatsResponse is Response.Success) {
//                                    Response.Success(
//                                        RestoreProgress(restoring, 4)
//                                    )
//                                } else {
//                                    Response.Error(ResponseError("Failed to refresh contacts and chats"))
//                                }
//                            )
//
//                        } catch (e: ParseException) {
//                            val msg =
//                                "Failed to convert date/time from Relay while processing Contacts"
//                            LOG.e(TAG, msg, e)
//                            emit(Response.Error(ResponseError(msg, e)))
//                        }
//
//                    }
//                    is LoadResponse.Loading -> {
//                        emit(loadResponse)
//                    }
//                }
//
//            }
        }
    }

    override suspend fun deleteContactById(contactId: ContactId): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()

        var owner: Contact? = accountOwner.value

        if (owner == null) {
            try {
                accountOwner.collect {
                    if (it != null) {
                        owner = it
                        throw Exception()
                    }
                }
            } catch (e: Exception) {
            }
            delay(25L)
        }

        if (owner?.id == null || owner!!.id == contactId) {
            val msg = "Account Owner was null, or deleteContactById was called for account owner."
            LOG.w(TAG, msg)
            return Response.Error(ResponseError(msg))
        }

        var deleteContactResponse: Response<Any, ResponseError> = Response.Success(Any())

        applicationScope.launch(mainImmediate) {
            // TODO V2 deleteContact

//            val response = networkQueryContact.deleteContact(contactId)
//            deleteContactResponse = response
//
//            if (response is Response.Success) {
//
//                chatLock.withLock {
//                    messageLock.withLock {
//                        contactLock.withLock {
//
//                            val chat: ChatDbo? =
//                                queries.chatGetConversationForContact(listOf(owner!!.id, contactId))
//                                    .executeAsOneOrNull()
//
//                            queries.transaction {
//                                deleteChatById(chat?.id, queries, latestMessageUpdatedTimeMap)
//                                deleteContactById(contactId, queries)
//                            }
//
//                        }
//                    }
//                }
//            }
        }.join()

        return deleteContactResponse
    }

    override fun createContact(
        contactAlias: ContactAlias,
        lightningNodePubKey: LightningNodePubKey,
        lightningRouteHint: LightningRouteHint?,
        contactKey: ContactKey?,
        photoUrl: PhotoUrl?
    ): Flow<LoadResponse<Any, ResponseError>> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        val postContactDto = PostContactDto(
            alias = contactAlias.value,
            public_key = lightningNodePubKey.value,
            status = ContactStatus.CONFIRMED.absoluteValue,
            route_hint = lightningRouteHint?.value,
            contact_key = contactKey?.value,
            photo_url = photoUrl?.value
        )

        val sharedFlow: MutableSharedFlow<Response<Boolean, ResponseError>> =
            MutableSharedFlow(1, 0)

        applicationScope.launch(mainImmediate) {

            // TODO V2 createContact

//            networkQueryContact.createContact(postContactDto).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        sharedFlow.emit(loadResponse)
//                    }
//                    is Response.Success -> {
//                        contactLock.withLock {
//                            withContext(io) {
//                                queries.transaction {
//                                    upsertContact(loadResponse.value, queries)
//                                }
//                            }
//                        }
//
//                        sharedFlow.emit(Response.Success(true))
//                    }
//                }
//            }

        }

        emit(LoadResponse.Loading)

        sharedFlow.asSharedFlow().firstOrNull().let { response ->
            if (response == null) {
                emit(Response.Error(ResponseError("")))
            } else {
                emit(response)
            }
        }
    }

    override suspend fun connectToContact(
        contactAlias: ContactAlias,
        lightningNodePubKey: LightningNodePubKey,
        lightningRouteHint: LightningRouteHint?,
        contactKey: ContactKey,
        message: String,
        photoUrl: PhotoUrl?,
        priceToMeet: Sat,
    ): Response<ContactId?, ResponseError> {
        var response: Response<ContactId?, ResponseError> = Response.Error(
            ResponseError("Something went wrong, please try again later")
        )

        applicationScope.launch(mainImmediate) {
            createContact(
                contactAlias,
                lightningNodePubKey,
                lightningRouteHint,
                contactKey,
                photoUrl
            ).collect { loadResponse ->
                Exhaustive@
                when (loadResponse) {
                    is LoadResponse.Loading -> {
                    }

                    is Response.Error -> {
                        response = loadResponse
                    }
                    is Response.Success -> {
                        val contact = getContactByPubKey(lightningNodePubKey).firstOrNull()

                        response = if (contact != null) {
                            val messageBuilder = SendMessage.Builder()
                            messageBuilder.setText(message)
                            messageBuilder.setContactId(contact.id)
                            messageBuilder.setPriceToMeet(priceToMeet)

                            sendMessage(
                                messageBuilder.build().first
                            )

                            Response.Success(contact.id)
                        } else {
                            Response.Error(
                                ResponseError("Contact not found")
                            )
                        }
                    }
                }
            }
        }.join()

        return response
    }

    override suspend fun updateOwner(
        alias: String?, privatePhoto: PrivatePhoto, tipAmount: Sat
    ): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()

        contactLock.withLock {
            queries.contactUpdateOwnerInfo(
                alias?.toContactAlias(),
                privatePhoto ?: PrivatePhoto.False,
                tipAmount
            )
        }

        connectManager.ownerInfoStateFlow.value?.let {
            connectManager.setOwnerInfo(
                it.copy(alias = alias)
            )
        }

        return Response.Success(Any())
    }

    override suspend fun updateContact(
        contactId: ContactId,
        alias: ContactAlias?,
        routeHint: LightningRouteHint?
    ): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()
        var response: Response<Any, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            try {
                // TODO V2 updateContact

//                networkQueryContact.updateContact(
//                    contactId,
//                    PutContactDto(
//                        alias = alias?.value,
//                        route_hint = routeHint?.value
//                    )
//                ).collect { loadResponse ->
//                    Exhaustive@
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//                        is Response.Error -> {
//                            response = loadResponse
//                        }
//                        is Response.Success -> {
//                            contactLock.withLock {
//                                queries.transaction {
//                                    updatedContactIds.add(ContactId(loadResponse.value.id))
//                                    upsertContact(loadResponse.value, queries)
//                                }
//                            }
//                            response = loadResponse
//
//                            LOG.d(TAG, "Contact has been successfully updated")
//                        }
//                    }
//                }
            } catch (e: Exception) {
                LOG.e(TAG, "Failed to update contact", e)

                response = Response.Error(ResponseError(e.message.toString()))
            }
        }.join()

        return response ?: Response.Error(ResponseError("Failed to update contact"))
    }

    override suspend fun forceKeyExchange(
        contactId: ContactId,
    ) {
        applicationScope.launch(mainImmediate) {
            try {

                // TODO V2 exchangeKeys
//                networkQueryContact.exchangeKeys(
//                    contactId,
//                ).collect { loadResponse ->
//                    Exhaustive@
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> { }
//                        is Response.Error -> { }
//                        is Response.Success -> { }
//                    }
//                }
            } catch (e: Exception) {
                LOG.e(TAG, "Failed to update contact", e)
            }
        }
    }

    override suspend fun updateOwnerDeviceId(deviceId: DeviceId): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()
        var response: Response<Any, ResponseError> = Response.Success(Any())

        try {
            accountOwner.collect { owner ->

                if (owner != null) {

                    if (owner.deviceId != deviceId) {

                        // TODO V2 updateContact

//                        networkQueryContact.updateContact(
//                            owner.id,
//                            PutContactDto(device_id = deviceId.value)
//                        ).collect { loadResponse ->
//                            Exhaustive@
//                            when (loadResponse) {
//                                is LoadResponse.Loading -> {
//                                }
//                                is Response.Error -> {
//                                    response = loadResponse
//                                    throw Exception()
//                                }
//                                is Response.Success -> {
//                                    contactLock.withLock {
//                                        queries.transaction {
//                                            upsertContact(loadResponse.value, queries)
//                                        }
//                                    }
//                                    LOG.d(TAG, "DeviceId has been successfully updated")
//
//                                    throw Exception()
//                                }
//                            }
//                        }
                    } else {
                        LOG.d(TAG, "DeviceId is up to date")
                        throw Exception()
                    }

                }

            }
        } catch (e: Exception) {
        }

        return response
    }

    @OptIn(RawPasswordAccess::class)
    override suspend fun updateOwnerNameAndKey(
        name: String,
        contactKey: Password
    ): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()
        var response: Response<Any, ResponseError> = Response.Success(Any())

        val publicKey = StringBuilder().let { sb ->
            sb.append(contactKey.value)
            sb.toString()
        }

        try {
            accountOwner.collect { owner ->
                if (owner != null) {

                    // TODO V2 updateContact

//                    networkQueryContact.updateContact(
//                        owner.id,
//                        PutContactDto(
//                            alias = name,
//                            contact_key = publicKey
//                        )
//                    ).collect { loadResponse ->
//                        Exhaustive@
//                        when (loadResponse) {
//                            is LoadResponse.Loading -> {
//                            }
//                            is Response.Error -> {
//                                response = loadResponse
//                                throw Exception()
//                            }
//                            is Response.Success -> {
//                                contactLock.withLock {
//                                    queries.transaction {
//                                        upsertContact(loadResponse.value, queries)
//                                    }
//                                }
//                                LOG.d(TAG, "Owner name and key has been successfully updated")
//
//                                throw Exception()
//                            }
//                        }
//                    }

                }

            }
        } catch (e: Exception) {
        }

        return response
    }

    override suspend fun updateProfilePic(
        path: Path,
        mediaType: MediaType,
        fileName: String,
        contentLength: Long?
    ): Response<Any, ResponseError> {
        var response: Response<Any, ResponseError> = Response.Success(true)
        val memeServerHost = MediaHost.DEFAULT

        applicationScope.launch(mainImmediate) {
            try {
                val token = memeServerTokenHandler.retrieveAuthenticationToken(memeServerHost)
                    ?: throw RuntimeException("MemeServerAuthenticationToken retrieval failure")

                val networkResponse = networkQueryMemeServer.uploadAttachment(
                    authenticationToken = token,
                    mediaType = mediaType,
                    path = path,
                    fileName = fileName,
                    contentLength = contentLength,
                    memeServerHost = memeServerHost,
                )

                Exhaustive@
                when (networkResponse) {
                    is Response.Error -> {
                        response = networkResponse
                    }
                    is Response.Success -> {
                        val newUrl =
                            PhotoUrl("https://${memeServerHost.value}/public/${networkResponse.value.muid}")

                        // TODO: if chatId method argument is null, update owner record

                        var owner = accountOwner.value

                        if (owner == null) {
                            try {
                                accountOwner.collect { contact ->
                                    if (contact != null) {
                                        owner = contact
                                        throw Exception()
                                    }
                                }
                            } catch (e: Exception) {
                            }
                            delay(25L)
                        }

                        owner?.let { nnOwner ->

                            val queries = coreDB.getSphinxDatabaseQueries()

                            contactLock.withLock {
                                withContext(io) {
                                    queries.contactUpdatePhotoUrl(
                                        newUrl,
                                        nnOwner.id,
                                    )
                                }
                            }

                            connectManager.ownerInfoStateFlow.value?.let {
                                connectManager.setOwnerInfo(
                                    it.copy(picture = newUrl.value)
                                )
                            }

                        } ?: throw IllegalStateException("Failed to retrieve account owner")
                    }
                }
            } catch (e: Exception) {
                response = Response.Error(
                    ResponseError("Failed to update Profile Picture", e)
                )
            }
        }.join()

        return response
    }

    override suspend fun toggleContactBlocked(contact: Contact): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError> = Response.Success(!contact.isBlocked())

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val currentBlockedValue = contact.blocked

            contactLock.withLock {
                withContext(io) {
                    queries.contactUpdateBlocked(
                        if (currentBlockedValue.isTrue()) Blocked.False else Blocked.True,
                        contact.id
                    )
                }
            }

            // TODO V2 toggleBlockedContact

//            networkQueryContact.toggleBlockedContact(
//                contact.id,
//                contact.blocked
//            ).collect { loadResponse ->
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {}
//
//                    is Response.Error -> {
//                        response = loadResponse
//
//                        contactLock.withLock {
//                            withContext(io) {
//                                queries.contactUpdateBlocked(
//                                    currentBlockedValue,
//                                    contact.id
//                                )
//                            }
//                        }
//                    }
//
//                    is Response.Success -> {}
//                }
//            }
        }.join()

        return response
    }

    override suspend fun setGithubPat(
        pat: String
    ): Response<Boolean, ResponseError> {

        var response: Response<Boolean, ResponseError> = Response.Error(
            ResponseError("generate Github PAT failed to execute")
        )

        return response
    }

    override suspend fun updateChatProfileInfo(
        chatId: ChatId,
        alias: ChatAlias?,
        profilePic: PublicAttachmentInfo?
    ): Response<ChatDto, ResponseError> {
        var response: Response<ChatDto, ResponseError> = Response.Error(
            ResponseError("updateChatProfileInfo failed to execute")
        )

        if (alias != null) {
            response = updateChatProfileAlias(chatId, alias)
        } else if (profilePic != null) {
            response = updateChatProfilePic(
                chatId,
                profilePic.path,
                profilePic.mediaType,
                profilePic.fileName,
                profilePic.contentLength
            )
        }

        return response
    }

    suspend fun updateChatProfilePic(
        chatId: ChatId,
        path: Path,
        mediaType: MediaType,
        fileName: String,
        contentLength: Long?
    ): Response<ChatDto, ResponseError> {
        var response: Response<ChatDto, ResponseError> = Response.Error(
            ResponseError("updateChatProfilePic failed to execute")
        )
        val memeServerHost = MediaHost.DEFAULT

        applicationScope.launch(mainImmediate) {
            try {
                val token = memeServerTokenHandler.retrieveAuthenticationToken(memeServerHost)
                    ?: throw RuntimeException("MemeServerAuthenticationToken retrieval failure")

                val networkResponse = networkQueryMemeServer.uploadAttachment(
                    authenticationToken = token,
                    mediaType = mediaType,
                    path = path,
                    fileName = fileName,
                    contentLength = contentLength,
                    memeServerHost = memeServerHost,
                )

                Exhaustive@
                when (networkResponse) {
                    is Response.Error -> {
                        response = networkResponse
                    }
                    is Response.Success -> {
                        val newUrl = PhotoUrl(
                            "https://${memeServerHost.value}/public/${networkResponse.value.muid}"
                        )
                    // TODO V2 updateChat

//                        networkQueryChat.updateChat(
//                            chatId,
//                            PutChatDto(
//                                my_photo_url = newUrl.value,
//                            )
//                        ).collect { loadResponse ->
//
//                            Exhaustive@
//                            when (loadResponse) {
//                                is LoadResponse.Loading -> {
//                                }
//                                is Response.Error -> {
//                                    response = loadResponse
//                                }
//                                is Response.Success -> {
//                                    response = loadResponse
//                                    val queries = coreDB.getSphinxDatabaseQueries()
//
//                                    chatLock.withLock {
//                                        withContext(io) {
//                                            queries.transaction {
//                                                upsertChat(
//                                                    loadResponse.value,
//                                                    chatSeenMap,
//                                                    queries,
//                                                    null
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            } catch (e: Exception) {
                response = Response.Error(
                    ResponseError("Failed to update Chat Profile", e)
                )
            }
        }.join()

        LOG.d(TAG, "Completed Upload Returning: $response")
        return response
    }

    private suspend fun updateChatProfileAlias(
        chatId: ChatId,
        alias: ChatAlias?
    ): Response<ChatDto, ResponseError> {
        var response: Response<ChatDto, ResponseError> = Response.Error(
            ResponseError("updateChatProfilePic failed to execute")
        )

        // TODO V2 updateChat

//        applicationScope.launch(mainImmediate) {
//            networkQueryChat.updateChat(
//                chatId,
//                PutChatDto(
//                    my_alias = alias?.value
//                )
//            ).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        response = loadResponse
//                    }
//                    is Response.Success -> {
//                        response = loadResponse
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        chatLock.withLock {
//                            withContext(io) {
//                                queries.transaction {
//                                    upsertChat(
//                                        loadResponse.value,
//                                        chatSeenMap,
//                                        queries,
//                                        null
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }.join()

        LOG.d(TAG, "Completed Upload Returning: $response")
        return response
    }

    /////////////////
    /// Lightning ///
    /////////////////
    @Suppress("RemoveExplicitTypeArguments")
    private val accountBalanceStateFlow: MutableStateFlow<NodeBalance?> by lazy {
        MutableStateFlow<NodeBalance?>(null)
    }
    private val balanceLock = Mutex()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getAccountBalanceStateFlow(): StateFlow<NodeBalance?> {
        balanceLock.withLock {

            if (accountBalanceStateFlow.value == null) {
                authenticationStorage
                    .getString(REPOSITORY_LIGHTNING_BALANCE, null)
                    ?.let { balanceJsonString ->
                        balanceJsonString.toLong().toNodeBalance()?.let { nodeBalance ->
                            accountBalanceStateFlow.value = nodeBalance
                        }
                    }
            }
        }
        return accountBalanceStateFlow.asStateFlow()
    }

    override val networkRefreshBalance: MutableStateFlow<Long?> by lazy {
        MutableStateFlow(null)
    }

    override suspend fun getActiveLSat(
        issuer: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ActiveLsatDto, ResponseError>> = flow {
        // TODO V2 getActiveLSat

//        networkQueryLightning.getActiveLSat(
//            issuer,
//            relayData
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
////                    emit(loadResponse)
//                }
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//                is Response.Success -> {
//                    emit(loadResponse)
//                }
//            }
//        }
    }

    override suspend fun signChallenge(
        challenge: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<SignChallengeDto, ResponseError>> = flow {
        // TODO V2 checkRoute signChallenge

//        networkQueryLightning.signChallenge(
//            challenge,
//            relayData
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
////                    emit(loadResponse)
//                }
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//                is Response.Success -> {
//                    emit(loadResponse)
//                }
//            }
//        }
    }

    override suspend fun payLSat(
        payLSatDto: PayLsatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<PayLsatResponseDto, ResponseError>> = flow {
        // TODO V2 payLsat

//        networkQueryLightning.payLSat(
//            payLSatDto,
//            relayData
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
////                    emit(loadResponse)
//                }
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//                is Response.Success -> {
//                    emit(loadResponse)
//                }
//            }
//        }
    }

    override suspend fun updateLSat(
        identifier: String,
        updateLSatDto: UpdateLsatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<String, ResponseError>> = flow {
        // TODO V2 updateLSat
//        networkQueryLightning.updateLSat(
//            identifier,
//            updateLSatDto,
//            relayData
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
////                    emit(loadResponse)
//                }
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//                is Response.Success -> {
//                    emit(loadResponse)
//                }
//            }
//        }
    }

    private var payInvoiceJob: Job? = null


    override suspend fun processLightningPaymentRequest(
        lightningPaymentRequest: LightningPaymentRequest,
        invoiceBolt11: InvoiceBolt11,
        callback: ((String) -> Unit)?
    ) {
        if (payInvoiceJob?.isActive == true) {
            return
        }

        payInvoiceJob = applicationScope.launch(mainImmediate) {
            val balance = getAccountBalanceStateFlow().firstOrNull() ?: return@launch

            val invoiceAmount = invoiceBolt11.getSatsAmount()?.value
            if (invoiceAmount != null && invoiceAmount > balance.balance.value) {
                return@launch
            }

            val invoicePayeePubKey = invoiceBolt11.getPubKey()
            val invoiceAmountMilliSat = invoiceBolt11.getMilliSatsAmount()?.value

            if (invoicePayeePubKey == null || invoiceAmountMilliSat == null) {
                return@launch
            }

            var payeeLspPubKey = invoiceBolt11.hop_hints?.getOrNull(0)?.substringBefore('_')
            val ownerLsp = getOwner()?.routeHint?.getLspPubKey()

            var payeeRouteHint = invoiceBolt11.hop_hints?.getOrNull((invoiceBolt11.hop_hints?.size ?: 1) - 1)
            var payeeHasRouteHint = payeeRouteHint != null


            if (payeeLspPubKey == null) {
                val contact = getContactByPubKey(invoicePayeePubKey).firstOrNull()
                payeeLspPubKey = contact?.routeHint?.getLspPubKey()
            }

            if (payeeLspPubKey == null || payeeLspPubKey != ownerLsp) {

                val isRouteAvailable = isRouteAvailable(
                    invoicePayeePubKey.value,
                    null,
                    invoiceAmountMilliSat
                )

                if (isRouteAvailable) {
                    payInvoice(
                        lightningPaymentRequest,
                        null,
                        null,
                        invoiceAmountMilliSat,
                        callback = {
                            callback?.invoke("Processing payment: This process could take up to 60 seconds. Please be patient")
                        }
                    )
                } else {
                    val routerUrl = serversUrls.getRouterUrl()

                    if (routerUrl == null) {
                        return@launch
                    }

                    networkQueryContact.getRoutingNodes(
                        routerUrl,
                        invoicePayeePubKey,
                        invoiceAmountMilliSat
                    ).collect { response ->
                        when (response) {
                            is LoadResponse.Loading -> {}
                            is Response.Error -> {
                                if (payeeHasRouteHint) {
                                    callback?.invoke("Error getting route")
                                } else {
                                    payInvoiceFromLSP(lightningPaymentRequest)
                                }
                            }

                            is Response.Success -> {
                                try {
                                    val routerPubKey = serversUrls.getRouterPubkey() ?: "true"

                                    payInvoice(
                                        lightningPaymentRequest,
                                        response.value,
                                        routerPubKey,
                                        invoiceAmountMilliSat
                                    )
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }
            } else {
                payInvoice(
                    lightningPaymentRequest,
                    null,
                    null,
                    invoiceAmountMilliSat,
                    isSphinxInvoice = payeeHasRouteHint,
                    callback = {
                        callback?.invoke("Processing payment: This process could take up to 60 seconds. Please be patient")
                    }

                )
            }
        }

    }

    override suspend fun getPersonData(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<PersonDataDto, ResponseError>> = flow {

        // TODO V2 getPersonData

//        networkQueryContact.getPersonData(
//            relayData
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
////                    emit(loadResponse)
//                }
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//                is Response.Success -> {
//                    emit(loadResponse)
//                }
//            }
//        }
    }

    override suspend fun createOwner(
        okKey: String,
        routeHint: String,
        shortChannelId: String,
        ownerAlias: String?
    ) {
        val queries = coreDB.getSphinxDatabaseQueries()
        val now = DateTime.nowUTC()

        val owner = Contact(
            id = ContactId(0L),
            routeHint = routeHint.toLightningRouteHint(),
            nodePubKey = okKey.toLightningNodePubKey(),
            nodeAlias = null,
            alias = ownerAlias?.toContactAlias(),
            photoUrl = null,
            privatePhoto = PrivatePhoto.False,
            isOwner = Owner.True,
            status = ContactStatus.AccountOwner,
            rsaPublicKey = null,
            deviceId = null,
            createdAt = now.toDateTime(),
            updatedAt = now.toDateTime(),
            fromGroup = ContactFromGroup.False,
            notificationSound = null,
            tipAmount = null,
            inviteId = null,
            inviteStatus = null,
            blocked = Blocked.False
        )
        contactLock.withLock {
            queries.transaction {
                upsertNewContact(owner, queries)
            }
        }
    }

    override suspend fun createNewContact(contact: NewContact) {
        val queries = coreDB.getSphinxDatabaseQueries()
        val now = DateTime.nowUTC()
        val contactId = getNewContactIndex().firstOrNull()?.value

        if (contactId == null || contactId < 0) {
            // Log and throw an exception if a valid contactId cannot be generated
            println("Error: Invalid contactId generated. Contact creation aborted.")
            throw IllegalArgumentException("Invalid contactId: Cannot create new contact without a valid contactId.")
        }

        val existingContact = contact.lightningNodePubKey
            ?.let { getContactByPubKey(it).firstOrNull() }

        val status = (contact.confirmed || existingContact?.status?.isConfirmed() == true)
        val contactStatus = if (status) ContactStatus.Confirmed else ContactStatus.Pending
        val chatStatus = if (status) ChatStatus.Approved else ChatStatus.Pending

        if (existingContact?.nodePubKey != null) {
            withContext(dispatchers.io) {
                contactLock.withLock {
                    queries.contactUpdateDetails(
                        contact.contactAlias,
                        contact.photoUrl,
                        contactStatus,
                        existingContact.id
                    )
                }
                chatLock.withLock {
                    queries.chatUpdateDetails(
                        contact.photoUrl,
                        chatStatus,
                        ChatId(existingContact.id.value)
                    )
                }
            }
        } else {
            // Initialize invite if applicable
            val invite = if (contact.invitePrice != null && contact.inviteCode != null) {
                Invite(
                    id = InviteId(contactId),
                    inviteString = InviteString(contact.inviteString ?: "null"),
                    paymentRequest = null,
                    contactId = ContactId(contactId),
                    status = InviteStatus.Pending,
                    price = contact.invitePrice,
                    createdAt = now.toDateTime(),
                    inviteCode = InviteCode(contact.inviteCode ?: "")
                )
            } else {
                null
            }

            // Create new contact and chat
            val newContact = Contact(
                id = ContactId(contactId),
                routeHint = contact.lightningRouteHint,
                nodePubKey = contact.lightningNodePubKey,
                nodeAlias = null,
                alias = existingContact?.alias ?: contact.contactAlias,
                photoUrl = contact.photoUrl,
                privatePhoto = PrivatePhoto.False,
                isOwner = Owner.False,
                status = contactStatus,
                rsaPublicKey = null,
                deviceId = null,
                createdAt = contact.createdAt ?: now.toDateTime(),
                updatedAt = contact.createdAt ?: now.toDateTime(),
                fromGroup = ContactFromGroup.False,
                notificationSound = null,
                tipAmount = null,
                inviteId = invite?.id,
                inviteStatus = invite?.status,
                blocked = Blocked.False
            )

            val newChat = Chat(
                id = ChatId(contactId),
                uuid = ChatUUID("${UUID.randomUUID()}"),
                name = ChatName(existingContact?.alias?.value ?: contact.contactAlias?.value ?: "unknown"),
                photoUrl = contact.photoUrl,
                type = ChatType.Conversation,
                status = chatStatus,
                contactIds = listOf(ContactId(0), ContactId(contactId)),
                isMuted = ChatMuted.False,
                createdAt = contact.createdAt ?: now.toDateTime(),
                groupKey = null,
                host = null,
                pricePerMessage = null,
                escrowAmount = null,
                unlisted = ChatUnlisted.False,
                privateTribe = ChatPrivate.False,
                ownerPubKey = null,
                seen = Seen.False,
                metaData = null,
                myPhotoUrl = null,
                myAlias = null,
                pendingContactIds = emptyList(),
                latestMessageId = null,
                contentSeenAt = null,
                notify = NotificationLevel.SeeAll,
                secondBrainUrl = null
            )

            withContext(dispatchers.io) {
                queries.transaction {
                    upsertNewContact(newContact, queries)
                }
                chatLock.withLock {
                    queries.transaction {
                        upsertNewChat(
                            newChat,
                            SynchronizedMap<ChatId, Seen>(),
                            queries,
                            newContact,
                            accountOwner.value?.nodePubKey
                        )
                    }
                }
                inviteLock.withLock {
                    invite?.let {
                        queries.transaction {
                            upsertNewInvite(it, queries)
                        }
                    }
                }
            }
        }
        println("CREATE_CONTACT: Contact and chat creation process completed.")
    }

    override suspend fun getNewContactIndex(): Flow<ContactId?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries().contactGetLastContactIndex()
                .asFlow()
                .mapToOneOrNull(io)
                .map { dbContactId ->
                    dbContactId?.value?.let {
                        ContactId(it + 1)
                    }
                }
        )
    }

    override suspend fun updateOwnerAlias(alias: ContactAlias) {
        val queries = coreDB.getSphinxDatabaseQueries()
        val now = DateTime.nowUTC().toDateTime()

        val updatedOwner = accountOwner.value?.copy(
            alias = alias,
            updatedAt = now
        )

        connectManager.ownerInfoStateFlow.value?.let { ownerInfo ->
            connectManager.setOwnerInfo(
                ownerInfo.copy(alias = alias.value)
            )
        }

        if (updatedOwner != null) {
            applicationScope.launch(mainImmediate) {
                contactLock.withLock {
                    queries.transaction {
                        upsertNewContact(updatedOwner, queries)
                    }
                }
            }
        }
    }

    override fun saveNewContactRegistered(
        msgSender: String,
        date: Long?,
        isRestoreAccount: Boolean,
    ) {
        applicationScope.launch(mainImmediate) {
            val contactInfo = msgSender.toMsgSender()
            val contact = NewContact(
                contactAlias = contactInfo.alias?.toContactAlias(),
                lightningNodePubKey = contactInfo.pubkey.toLightningNodePubKey(),
                lightningRouteHint = contactInfo.route_hint?.toLightningRouteHint(),
                photoUrl = contactInfo.photo_url?.toPhotoUrl(),
                confirmed = contactInfo.confirmed,
                null,
                inviteCode = contactInfo.code,
                invitePrice = null,
                null,
                date?.toDateTime()
            )

            if (contactInfo.code != null && !isRestoreAccount) {
                updateNewContactInvited(contact)
            } else {
                createNewContact(contact)
                println("CREATE_CONTACT: saveNewContactRegistered")
            }
        }
    }

    override fun updateNewContactInvited(contact: NewContact) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val invite = queries.inviteGetByCode(contact.inviteCode?.let { InviteCode(it) }).executeAsOneOrNull()

            if (invite != null) {
                val contactId = invite.contact_id

                queries.contactUpdateInvitee(
                    contact.contactAlias,
                    contact.photoUrl,
                    contact.lightningNodePubKey,
                    ContactStatus.Confirmed,
                    contact.lightningRouteHint,
                    ContactId(invite.id.value)
                )

                queries.inviteUpdateStatus(InviteStatus.Complete, invite.id)
                queries.chatUpdateNameAndStatus(
                    ChatName(contact.contactAlias?.value ?: "unknown"),
                    ChatStatus.Approved,
                    ChatId(contactId.value)
                )
                queries.dashboardUpdateConversation(
                    contact.contactAlias?.value,
                    contact.photoUrl,
                    contactId
                )

            } else {
            }
        }
    }


    ////////////////
    /// Messages ///
    ////////////////
    private val messageLock = Mutex()
    private val messageDboPresenterMapper: MessageDboPresenterMapper by lazy {
        MessageDboPresenterMapper(dispatchers)
    }

    @OptIn(RawPasswordAccess::class)
    private suspend fun decryptMessageContent(
        messageContent: MessageContent
    ): Response<UnencryptedByteArray, ResponseError> {
        return decryptString(messageContent.value)
    }

    @OptIn(RawPasswordAccess::class)
    private suspend fun decryptMediaKey(
        mediaKey: MediaKey
    ): Response<UnencryptedByteArray, ResponseError> {
        return decryptString(mediaKey.value)
    }

    @OptIn(RawPasswordAccess::class)
    private suspend fun decryptString(
        value: String
    ): Response<UnencryptedByteArray, ResponseError> {
        val privateKey: CharArray = authenticationCoreManager.getEncryptionKey()
            ?.privateKey
            ?.value
            ?: return Response.Error(
                ResponseError("EncryptionKey retrieval failed")
            )

        return rsa.decrypt(
            rsaPrivateKey = RsaPrivateKey(privateKey),
            text = EncryptedString(value),
            dispatcher = default
        )
    }

    @OptIn(UnencryptedDataAccess::class)
    private suspend fun mapMessageDboAndDecryptContentIfNeeded(
        queries: SphinxDatabaseQueries,
        messageDbo: MessageDbo,
        reactions: List<Message>? = null,
        thread: List<Message>? = null,
        purchaseItems: List<Message>? = null,
        replyMessage: ReplyUUID? = null,
    ): Message {

        val message: MessageDboWrapper = messageDbo.message_content?.let { messageContent ->

            if (
                messageDbo.type !is MessageType.KeySend &&
                messageDbo.message_content_decrypted == null
            ) {

                val response = decryptMessageContent(messageContent)

                Exhaustive@
                when (response) {
                    is Response.Error -> {
                        messageDboPresenterMapper.mapFrom(messageDbo).let { message ->
                            message._messageDecryptionException = response.exception
                            message._messageDecryptionError = true
                            message
                        }
                    }
                    is Response.Success -> {

                        val message: MessageDboWrapper =
                            messageDboPresenterMapper.mapFrom(messageDbo)

                        response.value
                            .toUnencryptedString(trim = false)
                            .value
                            .toMessageContentDecrypted()
                            ?.let { decryptedContent ->

                                messageLock.withLock {
                                    withContext(io) {
                                        queries.transaction {
                                            queries.messageUpdateContentDecrypted(
                                                decryptedContent,
                                                messageDbo.id
                                            )
                                        }
                                    }
                                }

                                message._messageContentDecrypted = decryptedContent

                            } ?: message.also { it._messageDecryptionError = true }

                        message
                    }
                }

            } else {

                messageDboPresenterMapper.mapFrom(messageDbo)

            }
        } ?: messageDboPresenterMapper.mapFrom(messageDbo)

        if (message.type.canContainMedia) {
            withContext(io) {
                queries.messageMediaGetById(message.id).executeAsOneOrNull()
            }?.let { mediaDbo ->

                mediaDbo.media_key?.let { key ->

                    mediaDbo.media_key_decrypted.let { decrypted ->

                        if (decrypted == null) {
                            val response = decryptMediaKey(MediaKey(key.value))

                            Exhaustive@
                            when (response) {
                                is Response.Error -> {
                                    MessageMediaDboWrapper(mediaDbo).also {
                                        it._mediaKeyDecrypted = null
                                        it._mediaKeyDecryptionError = true
                                        it._mediaKeyDecryptionException = response.exception
                                        message._messageMedia = it
                                    }
                                }
                                is Response.Success -> {

                                    response.value
                                        .toUnencryptedString(trim = false)
                                        .value
                                        .toMediaKeyDecrypted()
                                        .let { decryptedKey ->

                                            message._messageMedia = MessageMediaDboWrapper(mediaDbo)
                                                .also {
                                                    it._mediaKeyDecrypted = decryptedKey

                                                    if (decryptedKey == null) {
                                                        it._mediaKeyDecryptionError = true
                                                    } else {

                                                        messageLock.withLock {

                                                            withContext(io) {
                                                                queries.messageMediaUpdateMediaKeyDecrypted(
                                                                    decryptedKey,
                                                                    mediaDbo.id
                                                                )
                                                            }

                                                        }

                                                    }
                                                }
                                        }
                                }
                            }
                        } else {
                            message._messageMedia = MessageMediaDboWrapper(mediaDbo)
                        }

                    }

                } ?: message.also {
                    it._messageMedia = MessageMediaDboWrapper(mediaDbo)
                }

            } // else do nothing
        }

        if ((thread?.size ?: 0) > 1) {
            message._thread = thread
        }

        message._reactions = reactions
        message._purchaseItems = purchaseItems

        replyMessage?.value?.toMessageUUID()?.let { uuid ->
            queries.messageGetToShowByUUID(uuid).executeAsOneOrNull()?.let { replyDbo ->
                message._replyMessage = mapMessageDboAndDecryptContentIfNeeded(queries, replyDbo)
            }
        }

        return message
    }

    override fun getAllMessagesToShowByChatId(chatId: ChatId, limit: Long, chatThreadUUID: ThreadUUID?): Flow<List<Message>> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        emitAll(
            (
               if (chatThreadUUID != null) {
                   queries.messageGetAllMessagesByThreadUUID(chatId, listOf(chatThreadUUID))
            }
               else {
                   queries.messageGetAllToShowByChatId(chatId, limit)
               }
                    )
                .asFlow()
                .mapToList(io)
                .map { listMessageDbo ->
                    withContext(default) {
                        val reactionsMap: MutableMap<MessageUUID, ArrayList<Message>> =
                            LinkedHashMap(listMessageDbo.size)

                        val threadMap: MutableMap<MessageUUID, ArrayList<Message>> =
                            LinkedHashMap(listMessageDbo.size)

                        val purchaseItemsMap: MutableMap<MessageMUID, ArrayList<Message>> =
                            LinkedHashMap(listMessageDbo.size)

                        for (dbo in listMessageDbo) {
                            dbo.uuid?.let { uuid ->
                                reactionsMap[uuid] = ArrayList(0)
                            }
                            dbo.muid?.let { muid ->
                                purchaseItemsMap[muid] = ArrayList(0)
                            }
                            dbo.uuid?.let { uuid ->
                                threadMap[uuid] = ArrayList(0)
                            }
                        }

                        val replyUUIDs = reactionsMap.keys.map { ReplyUUID(it.value) }

                        val threadUUID = threadMap.keys.map { ThreadUUID(it.value) }

                        val purchaseItemsMUIDs = purchaseItemsMap.keys.map { MessageMUID(it.value) }

                        replyUUIDs.chunked(500).forEach { chunkedIds ->
                            queries.messageGetAllReactionsByUUID(
                                chatId,
                                chunkedIds,
                            ).executeAsList()
                                .let { response ->
                                    response.forEach { dbo ->
                                        dbo.reply_uuid?.let { uuid ->
                                            reactionsMap[MessageUUID(uuid.value)]?.add(
                                                mapMessageDboAndDecryptContentIfNeeded(queries, dbo)
                                            )
                                        }
                                    }
                                }
                        }

                        threadUUID.chunked(500).forEach { chunkedThreadUUID ->
                            queries.messageGetAllMessagesByThreadUUID(
                                chatId,
                                chunkedThreadUUID
                            ).executeAsList()
                                .let { response ->
                                    response.forEach { dbo ->
                                        dbo.thread_uuid?.let { uuid ->
                                            threadMap[MessageUUID(uuid.value)]?.add(
                                                mapMessageDboAndDecryptContentIfNeeded(
                                                    queries,
                                                    dbo
                                                )
                                            )
                                        }
                                    }
                                }
                        }

                        purchaseItemsMUIDs.chunked(500).forEach { chunkedMUIDs ->
                            queries.messageGetAllPurchaseItemsByMUID(
                                chatId,
                                chunkedMUIDs,
                            ).executeAsList()
                                .let { response ->
                                    response.forEach { dbo ->
                                        dbo.muid?.let { muid ->
                                            purchaseItemsMap[muid]?.add(
                                                mapMessageDboAndDecryptContentIfNeeded(queries, dbo)
                                            )
                                        }
                                        dbo.original_muid?.let { original_muid ->
                                            purchaseItemsMap[original_muid]?.add(
                                                mapMessageDboAndDecryptContentIfNeeded(queries, dbo)
                                            )
                                        }
                                    }
                                }
                        }

                        listMessageDbo.reversed().map { dbo ->
                            mapMessageDboAndDecryptContentIfNeeded(
                                queries,
                                dbo,
                                dbo.uuid?.let { reactionsMap[it] },
                                dbo.uuid?.let { threadMap[it] },
                                dbo.muid?.let { purchaseItemsMap[it] },
                                dbo.reply_uuid,
                            )
                        }

                    }
                }
        )
    }

    override fun getMessageById(messageId: MessageId): Flow<Message?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        emitAll(getMessageByIdImpl(messageId, queries))
    }

    override fun getMessagesByIds(messagesIds: List<MessageId>): Flow<List<Message?>> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetMessagesByIds(messagesIds)
                .asFlow()
                .mapToList(io)
                .map { listMessageDbo ->
                    listMessageDbo.map {
                        messageDboPresenterMapper.mapFrom(it)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override fun messageGetOkKeysByChatId(chatId: ChatId): Flow<List<MessageId>> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetOkKeysByChatId(chatId)
                .asFlow()
                .mapToList(io)
                .distinctUntilChanged()
        )
    }

    override fun getMessagesByPaymentHashes(paymentHashes: List<LightningPaymentHash>): Flow<List<Message?>> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetByPaymentHashes(paymentHashes)
                .asFlow()
                .mapToList(io)
                .map { listMessageDbo ->
                    listMessageDbo.map {
                        messageDboPresenterMapper.mapFrom(it)
                    }
                }
                .distinctUntilChanged()
        )
    }

    private fun getMessageByIdImpl(
        messageId: MessageId,
        queries: SphinxDatabaseQueries
    ): Flow<Message?> =
        queries.messageGetById(messageId)
            .asFlow()
            .mapToOneOrNull(io)
            .map {
                it?.let { messageDbo ->
                    mapMessageDboAndDecryptContentIfNeeded(queries, messageDbo)
                }
            }
            .distinctUntilChanged()

    override fun getTribeLastMemberRequestByContactId(
        contactId: ContactId,
        chatId: ChatId, ): Flow<Message?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        emitAll(
            queries.messageLastMemberRequestGetByContactId(contactId, chatId)
                .asFlow()
                .mapToOneOrNull(io)
                .map {
                    it?.let { messageDbo ->
                        mapMessageDboAndDecryptContentIfNeeded(queries, messageDbo)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override fun getMessageByUUID(messageUUID: MessageUUID): Flow<Message?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        emitAll(
            queries.messageGetByUUID(messageUUID)
                .asFlow()
                .mapToOneOrNull(io)
                .map {
                    it?.let { messageDbo ->
                        mapMessageDboAndDecryptContentIfNeeded(queries, messageDbo)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override suspend fun getAllMessagesByUUID(messageUUIDs: List<MessageUUID>): List<Message> {
        return coreDB.getSphinxDatabaseQueries()
            .messageGetAllByUUID(messageUUIDs)
            .executeAsList()
            .map { messageDboPresenterMapper.mapFrom(it) }
    }

    override fun updateMessageContentDecrypted(
        messageId: MessageId,
        messageContentDecrypted: MessageContentDecrypted
    ) {
        applicationScope.launch(io) {
            val queries = coreDB.getSphinxDatabaseQueries()
            messageLock.withLock {
                withContext(io) {
                    queries.transaction {
                        queries.messageUpdateContentDecrypted(
                            messageContentDecrypted,
                            messageId
                        )
                    }
                }
            }
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    private val chatSeenMap: SynchronizedMap<ChatId, Seen> by lazy {
        SynchronizedMap<ChatId, Seen>()
    }

    override fun readMessages(chatId: ChatId) {
        applicationScope.launch(mainImmediate) {
            sphinxNotificationManager.clearNotification(chatId.value)

            readMessagesImpl(
                chatId = chatId,
                queries = coreDB.getSphinxDatabaseQueries(),
                executeNetworkRequest = true
            )
        }
    }

    private suspend fun readMessagesImpl(
        chatId: ChatId,
        queries: SphinxDatabaseQueries,
        executeNetworkRequest: Boolean
    ) {
        chatLock.withLock {
            messageLock.withLock {
                withContext(io) {
                    chatSeenMap.withLock { map ->

                        if (map[chatId]?.isTrue() != true) {

                            queries.updateSeen(chatId)
                            LOG.d(TAG, "Chat [$chatId] marked as Seen")
                            map[chatId] = Seen.True

                            true
                        } else {
                            false
                        }
                    }
                }
            }

            val message = queries.messageGetMaxIdByChatId(chatId).executeAsOneOrNull()
            val contact = queries.contactGetById(ContactId(chatId.value)).executeAsOneOrNull()
            val chat = queries.chatGetById(chatId).executeAsOneOrNull()

            if (message != null) {
                if (contact != null) {
                    contact.node_pub_key?.value?.let { pubKey ->
                        connectManager.setReadMessage(pubKey, message.id.value)
                    }
                } else {
                    chat?.uuid?.value?.let { pubKey ->
                        connectManager.setReadMessage(pubKey, message.id.value)
                    }
                }
            }
        }
    }

    private val provisionalMessageLock = Mutex()

    private fun messageText(sendMessage: SendMessage): String? {
        try {
            if (sendMessage.giphyData != null) {
                return sendMessage.giphyData?.let {
                    "${GiphyData.MESSAGE_PREFIX}${it.toJson().toByteArray().encodeBase64()}"
                }
            }
        } catch (e: Exception) {
            LOG.e(TAG, "GiphyData toJson failed: ", e)
        }

        try {
            if (sendMessage.podcastClip != null) {
                return sendMessage.podcastClip?.let {
                    "${PodcastClip.MESSAGE_PREFIX}${it.toJson()}"
                }
            }
        } catch (e: Exception) {
            LOG.e(TAG, "PodcastClip toJson failed: ", e)
        }

        return sendMessage.text
    }

    @OptIn(RawPasswordAccess::class)
    override fun sendMessage(sendMessage: SendMessage?) {
        if (sendMessage == null) return

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val chat: Chat? = sendMessage.chatId?.let {
                getChatById(it)
            }

            val contact: Contact? = sendMessage.contactId?.let {
                getContactById(it).firstOrNull()
            }

            val owner: Contact? = accountOwner.value
                ?: let {
                    // TODO: Handle this better...
                    var owner: Contact? = null
                    try {
                        accountOwner.collect {
                            if (it != null) {
                                owner = it
                                throw Exception()
                            }
                        }
                    } catch (e: Exception) {
                    }
                    delay(25L)
                    owner
                }

            val ownerPubKey = owner?.nodePubKey

            if (owner == null) {
                LOG.w(TAG, "Owner returned null")
                return@launch
            }

            if (ownerPubKey == null) {
                LOG.w(TAG, "Owner's public key was null")
                return@launch
            }

            val message = messageText(sendMessage)
            val isPaidMessage: Boolean = (sendMessage.paidMessagePrice?.value ?: 0) > 0
            val media: AttachmentInfo? = sendMessage.attachmentInfo

            val pricePerMessage = chat?.pricePerMessage?.value ?: 0
            val escrowAmount = chat?.escrowAmount?.value ?: 0
            val priceToMeet = sendMessage.priceToMeet?.value ?: 0
            val messagePrice = (pricePerMessage + escrowAmount).toSat() ?: Sat(0)

            val messageType = when {
                (media != null) -> {
                    MessageType.Attachment
                }
                (sendMessage.groupAction != null) -> {
                    sendMessage.groupAction
                }
                (sendMessage.isBoost) -> {
                    MessageType.Boost
                }
                (sendMessage.isCall) -> {
                    MessageType.CallLink
                }
                (sendMessage.isTribePayment) -> {
                    MessageType.DirectPayment
                }
                else -> {
                    MessageType.Message
                }
            }

//            //If is tribe payment, reply UUID is sent to identify recipient. But it's not a response
            val replyUUID = when {
                (sendMessage.isTribePayment) -> {
                    null
                }
                else -> {
                    sendMessage.replyUUID
                }
            }

            val threadUUID = sendMessage.threadUUID

            val provisionalMessageId: MessageId? = chat?.let { chatDbo ->
                // Build provisional message and insert
                provisionalMessageLock.withLock {

                    val currentProvisionalId: MessageId? = withContext(io) {
                        queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
                    }
                    val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

                    withContext(io) {
                        queries.transaction {

                            // The following parms are set to null to make the upsert to work
                            // type, message_content, message_decrypted, status

                            queries.messageUpsert(
                                MessageStatus.Pending,
                                Seen.True,
                                sendMessage.senderAlias ?: chatDbo.myAlias?.value?.toSenderAlias(),
                                chatDbo.myPhotoUrl,
                                null,
                                replyUUID,
                                messageType,
                                null,
                                null,
                                Push.False,
                                null,
                                threadUUID,
                                null,
                                null,
                                provisionalId,
                                null,
                                chatDbo.id,
                                owner.id,
                                sendMessage.contactId,
                                sendMessage.tribePaymentAmount ?: sendMessage.paidMessagePrice ?: messagePrice ,
                                null,
                                null,
                                DateTime.nowUTC().toDateTime(),
                                null,
                                null,
                                message?.toMessageContentDecrypted() ?: sendMessage.text?.toMessageContentDecrypted(),
                                null,
                                false.toFlagged(),
                            )
                        }
                        provisionalId
                    }
                }
            }

            if (contact != null || chat != null) {
                if (media != null) {
                    val password = PasswordGenerator(MEDIA_KEY_SIZE).password
                    val token = memeServerTokenHandler.retrieveAuthenticationToken(MediaHost.DEFAULT)
                        ?: provisionalMessageId?.let { provId ->
                            withContext(io) {
                                queries.messageUpdateStatus(MessageStatus.Failed, provId)
                            }
                            return@launch
                        } ?: return@launch

                    val response = networkQueryMemeServer.uploadAttachmentEncrypted(
                        token,
                        media.mediaType,
                        media.filePath,
                        password,
                        MediaHost.DEFAULT,
                    )

                    when (response) {
                        is Response.Error -> {
                            LOG.e(TAG, response.message, response.exception)

                            provisionalMessageId?.let { provId ->
                                withContext(io) {
                                    queries.messageUpdateStatus(MessageStatus.Failed, provId)
                                }
                            }
                            return@launch
                        }

                        is Response.Success -> {
                            val pubKey = contact?.nodePubKey?.value ?: chat?.uuid?.value

                            pubKey?.let { nnPubKey ->

                                val amount = sendMessage.paidMessagePrice?.value

                                val mediaTokenValue = connectManager.generateMediaToken(
                                    nnPubKey,
                                    response.value.muid,
                                    MediaHost.DEFAULT.value,
                                    null,
                                    amount,
                                )

                                val mediaKey = MediaKey(password.value.copyOf().joinToString(""))

                                queries.messageMediaUpsert(
                                    mediaKey,
                                    media.mediaType,
                                    mediaTokenValue?.toMediaToken() ?: MediaToken.PROVISIONAL_TOKEN,
                                    provisionalMessageId ?: MessageId(Long.MIN_VALUE),
                                    chat?.id ?: ChatId(ChatId.NULL_CHAT_ID.toLong()),
                                    MediaKeyDecrypted(password.value.copyOf().joinToString("")),
                                    media.filePath,
                                    sendMessage.attachmentInfo?.fileName
                                )

                                sendNewMessage(
                                    contact?.nodePubKey?.value ?: chat?.uuid?.value ?: "",
                                    message ?: sendMessage.text ?: "",
                                    media,
                                    mediaTokenValue?.toMediaToken(),
                                    if (isPaidMessage && chat?.isTribe() == false) null else mediaKey,
                                    messageType,
                                    provisionalMessageId,
                                    sendMessage.tribePaymentAmount ?: messagePrice,
                                    replyUUID,
                                    threadUUID,
                                    chat?.isTribe() ?: false,
                                    sendMessage.memberPubKey
                                )

                                LOG.d("MQTT_MESSAGES", "Media Message was sent. mediatoken=$mediaTokenValue mediakey$mediaKey" )
                            }
                        }
                    }
                } else {
                    sendNewMessage(
                        contact?.nodePubKey?.value ?: chat?.uuid?.value ?: "",
                        message ?: sendMessage.text ?: "",
                        null,
                        null,
                        null,
                        messageType,
                        provisionalMessageId,
                        sendMessage.tribePaymentAmount ?: messagePrice,
                        replyUUID,
                        threadUUID,
                        chat?.isTribe() ?: false,
                        sendMessage.memberPubKey
                    )
                }
            }
        }
    }

    fun sendNewMessage(
        contact: String,
        messageContent: String,
        attachmentInfo: AttachmentInfo?,
        mediaToken: MediaToken?,
        mediaKey: MediaKey?,
        messageType: MessageType?,
        provisionalId: MessageId?,
        amount: Sat?,
        replyUUID: ReplyUUID?,
        threadUUID: ThreadUUID?,
        isTribe: Boolean,
        memberPubKey: LightningNodePubKey?
    ) {
        val newMessage = chat.sphinx.wrapper.mqtt.Message(
            messageContent,
            null,
            mediaToken?.value,
            mediaKey?.value,
            attachmentInfo?.mediaType?.value,
            replyUUID?.value,
            threadUUID?.value,
            memberPubKey?.value,
            null
        ).toJson()

        provisionalId?.value?.let {
            connectManager.sendMessage(
                newMessage,
                contact,
                it,
                messageType?.value ?: 0,
                amount?.value,
                isTribe
            )
        }
    }


//    // TODO: Rework to handle different message types
//    @OptIn(RawPasswordAccess::class)
//    override fun sendMessage(sendMessage: SendMessage?) {
//        if (sendMessage == null) return
//
//        applicationScope.launch(mainImmediate) {
//
//            val queries = coreDB.getSphinxDatabaseQueries()
//
//            // TODO: Update SendMessage to accept a Chat && Contact instead of just IDs
//            val chat: Chat? = sendMessage.chatId?.let {
//                getChatByIdFlow(it).firstOrNull()
//            }
//
//            val contact: Contact? = sendMessage.contactId?.let {
//                getContactById(it).firstOrNull()
//            }
//
//            val owner: Contact? = accountOwner.value
//                ?: let {
//                    // TODO: Handle this better...
//                    var owner: Contact? = null
//                    try {
//                        accountOwner.collect {
//                            if (it != null) {
//                                owner = it
//                                throw Exception()
//                            }
//                        }
//                    } catch (e: Exception) {
//                    }
//                    delay(25L)
//                    owner
//                }
//
//            val ownerPubKey = owner?.rsaPublicKey
//
//            if (owner == null) {
//                LOG.w(TAG, "Owner returned null")
//                return@launch
//            }
//
//            if (ownerPubKey == null) {
//                LOG.w(TAG, "Owner's RSA public key was null")
//                return@launch
//            }
//
//            // encrypt text
//            val message: Pair<MessageContentDecrypted, MessageContent>? =
//                messageText(sendMessage)?.let { msgText ->
//
//                    val response = rsa.encrypt(
//                        ownerPubKey,
//                        UnencryptedString(msgText),
//                        formatOutput = false,
//                        dispatcher = default,
//                    )
//
//                    Exhaustive@
//                    when (response) {
//                        is Response.Error -> {
//                            LOG.e(TAG, response.message, response.exception)
//                            null
//                        }
//                        is Response.Success -> {
//                            Pair(
//                                MessageContentDecrypted(msgText),
//                                MessageContent(response.value.value)
//                            )
//                        }
//                    }
//                }
//
//            // media attachment
//            val media: Triple<Password, MediaKey, AttachmentInfo>? =
//                if (sendMessage.giphyData == null) {
//                    sendMessage.attachmentInfo?.let { info ->
//                        val password = PasswordGenerator(MEDIA_KEY_SIZE).password
//
//                        val response = rsa.encrypt(
//                            ownerPubKey,
//                            UnencryptedString(password.value.joinToString("")),
//                            formatOutput = false,
//                            dispatcher = default,
//                        )
//
//                        Exhaustive@
//                        when (response) {
//                            is Response.Error -> {
//                                LOG.e(TAG, response.message, response.exception)
//                                null
//                            }
//                            is Response.Success -> {
//                                Triple(password, MediaKey(response.value.value), info)
//                            }
//                        }
//                    }
//                } else {
//                    null
//                }
//
//            if (message == null && media == null && !sendMessage.isTribePayment) {
//                return@launch
//            }
//
//            val pricePerMessage = chat?.pricePerMessage?.value ?: 0
//            val escrowAmount = chat?.escrowAmount?.value ?: 0
//            val priceToMeet = sendMessage.priceToMeet?.value ?: 0
//            val messagePrice = (pricePerMessage + escrowAmount + priceToMeet).toSat() ?: Sat(0)
//
//            val messageType = when {
//                (media != null) -> {
//                    MessageType.Attachment
//                }
//                (sendMessage.isBoost) -> {
//                    MessageType.Boost
//                }
//                (sendMessage.isTribePayment) -> {
//                    MessageType.DirectPayment
//                }
//                (sendMessage.isCall) -> {
//                    MessageType.CallLink
//                }
//                else -> {
//                    MessageType.Message
//                }
//            }
//
//            //If is tribe payment, reply UUID is sent to identify recipient. But it's not a response
//            val replyUUID = when {
//                (sendMessage.isTribePayment) -> {
//                    null
//                }
//                else -> {
//                    sendMessage.replyUUID
//                }
//            }
//
//            val threadUUID = when {
//                (sendMessage.isTribePayment) -> {
//                    null
//                }
//                else -> {
//                    sendMessage.threadUUID
//                }
//            }
//
//            val provisionalMessageId: MessageId? = chat?.let { chatDbo ->
//                // Build provisional message and insert
//                provisionalMessageLock.withLock {
//                    val currentProvisionalId: MessageId? = withContext(io) {
//                        queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
//                    }
//
//                    val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)
//
//                    withContext(io) {
//
//                        queries.transaction {
//
//                            if (media != null) {
//                                queries.messageMediaUpsert(
//                                    media.second,
//                                    media.third.mediaType,
//                                    MediaToken.PROVISIONAL_TOKEN,
//                                    provisionalId,
//                                    chatDbo.id,
//                                    MediaKeyDecrypted(media.first.value.joinToString("")),
//                                    media.third.filePath,
//                                    media.third.fileName
//                                )
//                            }
//
//                            queries.messageUpsert(
//                                MessageStatus.Pending,
//                                Seen.True,
//                                chatDbo.myAlias?.value?.toSenderAlias(),
//                                chatDbo.myPhotoUrl,
//                                null,
//                                replyUUID,
//                                messageType,
//                                null,
//                                null,
//                                Push.False,
//                                null,
//                                threadUUID,
//                                null,
//                                null,
//                                provisionalId,
//                                null,
//                                chatDbo.id,
//                                owner.id,
//                                sendMessage.contactId,
//                                messagePrice,
//                                null,
//                                null,
//                                DateTime.nowUTC().toDateTime(),
//                                null,
//                                message?.second,
//                                message?.first,
//                                null,
//                                false.toFlagged()
//                            )
//
//                            if (media != null) {
//                                queries.messageMediaUpsert(
//                                    media.second,
//                                    media.third.mediaType,
//                                    MediaToken.PROVISIONAL_TOKEN,
//                                    provisionalId,
//                                    chatDbo.id,
//                                    MediaKeyDecrypted(media.first.value.joinToString("")),
//                                    media.third.filePath,
//                                    media.third.fileName
//                                )
//                            }
//                        }
//                    }
//
//                    provisionalId
//                }
//            }
//
//            val isPaidTextMessage =
//                sendMessage.attachmentInfo?.mediaType?.isSphinxText == true &&
//                        sendMessage.paidMessagePrice?.value ?: 0 > 0
//
//            val messageContent: String? = if (isPaidTextMessage) null else message?.second?.value
//
//            val remoteTextMap: Map<String, String>? =
//                if (isPaidTextMessage) null else getRemoteTextMap(
//                    UnencryptedString(message?.first?.value ?: ""),
//                    contact,
//                    chat
//                )
//
//            val mediaKeyMap: Map<String, String>? = if (media != null) {
//                getMediaKeyMap(
//                    owner.id,
//                    media.second,
//                    UnencryptedString(media.first.value.joinToString("")),
//                    contact,
//                    chat
//                )
//            } else {
//                null
//            }
//
//            val postMemeServerDto: PostMemeServerUploadDto? = if (media != null) {
//                val token = memeServerTokenHandler.retrieveAuthenticationToken(MediaHost.DEFAULT)
//                    ?: provisionalMessageId?.let { provId ->
//                        withContext(io) {
//                            queries.messageUpdateStatus(MessageStatus.Failed, provId)
//                        }
//
//                        return@launch
//                    } ?: return@launch
//
//                val response = networkQueryMemeServer.uploadAttachmentEncrypted(
//                    token,
//                    media.third.mediaType,
//                    media.third.filePath,
//                    media.first,
//                    MediaHost.DEFAULT,
//                )
//
//                Exhaustive@
//                when (response) {
//                    is Response.Error -> {
//                        LOG.e(TAG, response.message, response.exception)
//
//                        provisionalMessageId?.let { provId ->
//                            withContext(io) {
//                                queries.messageUpdateStatus(MessageStatus.Failed, provId)
//                            }
//                        }
//
//                        return@launch
//                    }
//                    is Response.Success -> {
//                        response.value
//                    }
//                }
//            } else {
//                null
//            }
//
//            val amount = messagePrice.value + (sendMessage.tribePaymentAmount ?: Sat(0)).value
//
//            val postMessageDto: PostMessageDto = try {
//                PostMessageDto(
//                    sendMessage.chatId?.value,
//                    sendMessage.contactId?.value,
//                    amount,
//                    messagePrice.value,
//                    sendMessage.replyUUID?.value,
//                    messageContent,
//                    remoteTextMap,
//                    mediaKeyMap,
//                    postMemeServerDto?.mime,
//                    postMemeServerDto?.muid,
//                    sendMessage.paidMessagePrice?.value,
//                    sendMessage.isBoost,
//                    sendMessage.isCall,
//                    sendMessage.isTribePayment,
//                    sendMessage.threadUUID?.value
//                )
//            } catch (e: IllegalArgumentException) {
//                LOG.e(TAG, "Failed to create PostMessageDto", e)
//
//                provisionalMessageId?.let { provId ->
//                    withContext(io) {
//                        queries.messageUpdateStatus(MessageStatus.Failed, provId)
//                    }
//                }
//
//                return@launch
//            }
//
//            sendMessage(
//                provisionalMessageId,
//                postMessageDto,
//                message?.first,
//                media
//            )
//        }
//    }

    private suspend fun getRemoteTextMap(
        unencryptedString: UnencryptedString?,
        contact: Contact?,
        chat: Chat?,
    ): Map<String, String>? {

        return if (unencryptedString != null) {
            contact?.id?.let { nnContactId ->
                // we know it's a conversation as the contactId is always sent
                contact.rsaPublicKey?.let { pubKey ->

                    val response = rsa.encrypt(
                        pubKey,
                        unencryptedString,
                        formatOutput = false,
                        dispatcher = default,
                    )

                    Exhaustive@
                    when (response) {
                        is Response.Error -> {
                            LOG.e(TAG, response.message, response.exception)
                            null
                        }
                        is Response.Success -> {
                            mapOf(Pair(nnContactId.value.toString(), response.value.value))
                        }
                    }
                }

            } ?: chat?.groupKey?.value?.let { rsaPubKeyString ->
                val response = rsa.encrypt(
                    RsaPublicKey(rsaPubKeyString.toCharArray()),
                    unencryptedString,
                    formatOutput = false,
                    dispatcher = default,
                )

                Exhaustive@
                when (response) {
                    is Response.Error -> {
                        LOG.e(TAG, response.message, response.exception)
                        null
                    }
                    is Response.Success -> {
                        mapOf(Pair("chat", response.value.value))
                    }
                }
            }
        } else {
            null
        }
    }

    private suspend fun getMediaKeyMap(
        ownerId: ContactId,
        mediaKey: MediaKey,
        unencryptedMediaKey: UnencryptedString?,
        contact: Contact?,
        chat: Chat?,
    ): Map<String, String>? {

        return if (unencryptedMediaKey != null) {
            val map: MutableMap<String, String> = LinkedHashMap(2)

            map[ownerId.value.toString()] = mediaKey.value

            contact?.id?.let { nnContactId ->
                // we know it's a conversation as the contactId is always sent
                contact.rsaPublicKey?.let { pubKey ->

                    val response = rsa.encrypt(
                        pubKey,
                        unencryptedMediaKey,
                        formatOutput = false,
                        dispatcher = default,
                    )

                    Exhaustive@
                    when (response) {
                        is Response.Error -> {
                            LOG.e(TAG, response.message, response.exception)
                        }
                        is Response.Success -> {
                            map[nnContactId.value.toString()] = response.value.value
                        }
                    }
                }

            } ?: chat?.groupKey?.value?.let { rsaPubKeyString ->
                val response = rsa.encrypt(
                    RsaPublicKey(rsaPubKeyString.toCharArray()),
                    unencryptedMediaKey,
                    formatOutput = false,
                    dispatcher = default,
                )

                Exhaustive@
                when (response) {
                    is Response.Error -> {
                        LOG.e(TAG, response.message, response.exception)
                    }
                    is Response.Success -> {
                        map["chat"] = response.value.value
                    }
                }
            }

            map
        } else {
            null
        }
    }

    @OptIn(RawPasswordAccess::class)
    suspend fun sendMessage(
        provisionalMessageId: MessageId?,
        postMessageDto: PostMessageDto,
        messageContentDecrypted: MessageContentDecrypted?,
        media: Triple<Password, MediaKey, AttachmentInfo>?,
    ) {
        val queries = coreDB.getSphinxDatabaseQueries()
        // TODO V2 sendMessage

//        networkQueryMessage.sendMessage(postMessageDto).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
//                }
//                is Response.Error -> {
//                    LOG.e(TAG, loadResponse.message, loadResponse.exception)
//
//                    messageLock.withLock {
//                        provisionalMessageId?.let { provId ->
//                            withContext(io) {
//                                queries.messageUpdateStatus(MessageStatus.Failed, provId)
//                            }
//                        }
//                    }
//
//                }
//                is Response.Success -> {
//
//                    loadResponse.value.apply {
//                        if (media != null) {
//                            setMediaKeyDecrypted(media.first.value.joinToString(""))
//                            setMediaLocalFile(media.third.filePath)
//                        }
//
//                        if (messageContentDecrypted != null) {
//                            setMessageContentDecrypted(messageContentDecrypted.value)
//                        }
//                    }
//
//                    chatLock.withLock {
//                        messageLock.withLock {
//                            contactLock.withLock {
//                                withContext(io) {
//                                    queries.transaction {
//                                        // chat is returned only if this is the
//                                        // first message sent to a new contact
//                                        loadResponse.value.chat?.let { chatDto ->
//                                            upsertChat(
//                                                chatDto,
//                                                chatSeenMap,
//                                                queries,
//                                                loadResponse.value.contact,
//                                            )
//                                        }
//
//                                        loadResponse.value.contact?.let { contactDto ->
//                                            upsertContact(contactDto, queries)
//                                        }
//
//                                        upsertMessage(
//                                            loadResponse.value,
//                                            queries,
//                                            media?.third?.fileName
//                                        )
//
//                                        provisionalMessageId?.let { provId ->
//                                            deleteMessageById(provId, queries)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun resendMessage(message: Message, chat: Chat) {

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val pricePerMessage = chat.pricePerMessage?.value ?: 0
            val escrowAmount = chat.escrowAmount?.value ?: 0
            val messagePrice = (pricePerMessage + escrowAmount).toSat() ?: Sat(0)

            val contact: Contact? = if (chat.type.isConversation()) {
                chat.contactIds.elementAtOrNull(1)?.let { contactId ->
                    getContactById(contactId).firstOrNull()
                }
            } else {
                null
            }

            val remoteTextMap: Map<String, String>? = getRemoteTextMap(
                UnencryptedString(message.messageContentDecrypted?.value ?: ""),
                contact,
                chat
            )

            val postMessageDto: PostMessageDto = try {
                PostMessageDto(
                    message.chatId.value,
                    contact?.id?.value,
                    messagePrice.value,
                    messagePrice.value,
                    message.replyUUID?.value,
                    message.messageContentDecrypted?.value,
                    remoteTextMap,
                    null,
                    message.messageMedia?.mediaType?.value,
                    message.messageMedia?.muid?.value,
                    null,
                    false,
                    thread_uuid = message.threadUUID?.value
                )
            } catch (e: IllegalArgumentException) {
                LOG.e(TAG, "Failed to create PostMessageDto", e)

                withContext(io) {
                    queries.messageUpdateStatus(MessageStatus.Failed, message.id)
                }

                return@launch
            }

            sendMessage(
                message.id,
                postMessageDto,
                message.messageContentDecrypted,
                null
            )
        }
    }

    override fun flagMessage(message: Message, chat: Chat) {
        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            messageLock.withLock {
                withContext(io) {
                    queries.messageUpdateFlagged(
                        true.toFlagged(),
                        message.id
                    )
                }
            }

            val supportContactPubKey = LightningNodePubKey(
                "023d70f2f76d283c6c4e58109ee3a2816eb9d8feb40b23d62469060a2b2867b77f"
            )

            getContactByPubKey(supportContactPubKey).firstOrNull()?.let { supportContact ->
                val messageSender = getContactById(message.sender).firstOrNull()

                var flagMessageContent = "Message Flagged\n- Message: ${message.uuid?.value ?: "Empty Message UUID"}\n- Sender: ${messageSender?.nodePubKey?.value ?: "Empty Sender"}"

                if (chat.isTribe()) {
                    flagMessageContent += "\n- Tribe: ${chat.uuid.value}"
                }

                val messageBuilder = SendMessage.Builder()
                messageBuilder.setText(flagMessageContent.trimIndent())

                messageBuilder.setContactId(supportContact.id)

                getConversationByContactIdFlow(supportContact.id).firstOrNull()?.let { supportContactChat ->
                    messageBuilder.setChatId(supportContactChat.id)
                }

                sendMessage(
                    messageBuilder.build().first
                )
            }
        }
    }

    override suspend fun deleteMessage(message: Message) {
        var response: Response<Any, ResponseError> = Response.Success(true)

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val contact = getContactById(ContactId(message.chatId.value)).firstOrNull()
            val chatTribe = getChatById(message.chatId)

            if (message.id.isProvisionalMessage) {
                messageLock.withLock {
                    withContext(io) {
                        queries.transaction {
                            deleteMessageById(message.id, queries)
                        }
                    }
                }
            } else {
                messageLock.withLock {
                    withContext(io) {
                        queries.messageUpdateStatus(MessageStatus.Deleted, message.id)
                    }
                }

                val newMessage = chat.sphinx.wrapper.mqtt.Message(
                    "",
                    null,
                    null,
                    null,
                    null,
                    message.uuid?.value,
                    null,
                    null,
                    null
                ).toJson()

                val contactPubKey = contact?.nodePubKey?.value ?: chatTribe?.uuid?.value
                val isTribe = (chatTribe != null)

                if (contactPubKey != null) {
                    connectManager.deleteMessage(
                        newMessage,
                        contactPubKey,
                        isTribe
                    )
                }
            }
        }
    }

    override suspend fun deleteAllMessagesAndPubKey(pubKey: String, chatId: ChatId) {
        val messagesIds = messageGetOkKeysByChatId(chatId).firstOrNull()
        if (messagesIds != null) {
            connectManager.deleteContactMessages(messagesIds.map { it.value })
            connectManager.deletePubKeyMessages(pubKey)
        }
    }

    override suspend fun sendPayment(
        sendPayment: SendPayment?
    ): Response<Any, ResponseError> {

        var response: Response<Any, ResponseError> = Response.Success(true)

        if (sendPayment == null) {
            response = Response.Error(
                ResponseError("Payment params cannot be null")
            )
            return response
        }

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val contact: ContactDbo? = sendPayment.contactId?.let {
                withContext(io) {
                    queries.contactGetById(it).executeAsOneOrNull()
                }
            }

            val owner: Contact? = accountOwner.value
                ?: let {
                    // TODO: Handle this better...
                    var owner: Contact? = null
                    try {
                        accountOwner.collect {
                            if (it != null) {
                                owner = it
                                throw Exception()
                            }
                        }
                    } catch (e: Exception) {
                    }
                    delay(25L)
                    owner
                }

            if (owner == null) {
                response = Response.Error(
                    ResponseError("Owner cannot be null")
                )
                return@launch
            }

            val text = sendPayment.text

            val currentProvisionalId: MessageId? = withContext(io) {
                queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
            }
            val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

            val newPayment = NewMessage(
                id = provisionalId,
                uuid = null,
                chatId = sendPayment.chatId ?: ChatId(ChatId.NULL_CHAT_ID.toLong()),
                type = MessageType.DirectPayment,
                sender = owner.id,
                receiver = null,
                amount = Sat(sendPayment.amount),
                date = DateTime.nowUTC().toDateTime(),
                expirationDate = null,
                messageContent = null,
                status = MessageStatus.Pending,
                seen = Seen.True,
                senderAlias = null,
                senderPic = null,
                originalMUID = sendPayment.paymentTemplate?.muid?.toMessageMUID(),
                replyUUID = null,
                flagged = false.toFlagged(),
                recipientAlias = null,
                recipientPic = null,
                person = null,
                threadUUID = null,
                errorMessage = null,
                messageContentDecrypted = text?.toMessageContentDecrypted(),
                messageDecryptionError = false,
                messageDecryptionException = null,
                messageMedia = null,
                feedBoost = null,
                callLinkMessage = null,
                podcastClip = null,
                giphyData = null,
                reactions = null,
                purchaseItems = null,
                replyMessage = null,
                thread = null
            )

            var mediaTokenValue: String? = null

            sendPayment.paymentTemplate?.let { template ->

                mediaTokenValue = connectManager.generateMediaToken(
                    contact?.node_pub_key?.value ?: "",
                    sendPayment.paymentTemplate?.muid ?: "",
                    MediaHost.DEFAULT.value,
                    sendPayment.paymentTemplate?.getDimensions(),
                    null
                )

                queries.messageMediaUpsert(
                    null,
                    MediaType.IMAGE.toMediaType(),
                    mediaTokenValue?.toMediaToken() ?: MediaToken.PROVISIONAL_TOKEN,
                    provisionalId,
                    ChatId(contact?.id?.value ?: ChatId.NULL_CHAT_ID.toLong()),
                    null,
                    null,
                    null
                )

            }

            val newPaymentMessage = chat.sphinx.wrapper.mqtt.Message(
                text,
                null,
                mediaTokenValue,
                null,
                MediaType.IMAGE,
                null,
                null,
                null,
                null
            ).toJson()


            chatLock.withLock {
                messageLock.withLock {
                    withContext(io) {

                        queries.transaction {
                            upsertNewMessage(newPayment, queries, null)
                        }
                    }
                }
            }

            contact?.let { nnContact ->
                connectManager.sendMessage(
                    newPaymentMessage,
                    contact.node_pub_key?.value ?: "",
                    provisionalId.value,
                    MessageType.DIRECT_PAYMENT,
                    sendPayment.amount,
                )
            }

        }.join()

        return response
    }

    override suspend fun boostMessage(
        chatId: ChatId,
        pricePerMessage: Sat,
        escrowAmount: Sat,
        messageUUID: MessageUUID,
    ): Response<Any, ResponseError> {

        var response: Response<Any, ResponseError> = Response.Success(true)

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val contact = getContactById(ContactId(chatId.value)).firstOrNull()
            val currentChat = getChatById(chatId)

            val owner: Contact = accountOwner.value.let {
                if (it != null) {
                    it
                } else {
                    var owner: Contact? = null
                    val retrieveOwnerJob = applicationScope.launch(mainImmediate) {
                        try {
                            accountOwner.collect { contact ->
                                if (contact != null) {
                                    owner = contact
                                    throw Exception()
                                }
                            }
                        } catch (e: Exception) {
                        }
                        delay(20L)
                    }

                    delay(200L)
                    retrieveOwnerJob.cancelAndJoin()

                    owner ?: let {
                        response = Response.Error(
                            ResponseError("Owner Contact returned null")
                        )
                        return@launch
                    }
                }
            }

            val currentProvisionalId: MessageId? = withContext(io) {
                queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
            }
            val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

            val newBoost = NewMessage(
                id = provisionalId,
                uuid = null,
                chatId = chatId,
                type = MessageType.Boost,
                sender = owner.id,
                receiver = null,
                amount = owner.tipAmount ?: Sat(20L),
                date = DateTime.nowUTC().toDateTime(),
                expirationDate = null,
                messageContent = null,
                status = MessageStatus.Confirmed,
                seen = Seen.True,
                senderAlias = null,
                senderPic = null,
                originalMUID = null,
                replyUUID = ReplyUUID(messageUUID.value),
                flagged = false.toFlagged(),
                recipientAlias = null,
                recipientPic = null,
                person = null,
                threadUUID = null,
                errorMessage = null,
                tagMessage = null,
                messageContentDecrypted = null,
                messageDecryptionError = false,
                messageDecryptionException = null,
                messageMedia = null,
                feedBoost = null,
                callLinkMessage = null,
                podcastClip = null,
                giphyData = null,
                reactions = null,
                purchaseItems = null,
                replyMessage = null,
                thread = null
            )

            val newBoostMessage = chat.sphinx.wrapper.mqtt.Message(
                null,
                null,
                null,
                null,
                null,
                messageUUID.value,
                null,
                null,
                null
            ).toJson()

            chatLock.withLock {
                messageLock.withLock {
                    withContext(io) {

                        queries.transaction {
                            upsertNewMessage(newBoost, queries, null)
                        }
                    }

                    queries.transaction {
                        updateChatNewLatestMessage(
                            newBoost,
                            chatId,
                            latestMessageUpdatedTimeMap,
                            queries
                        )
                    }
                }
            }

            val contactPubKey = contact?.nodePubKey?.value ?: currentChat?.uuid?.value

            if (contactPubKey != null) {
                connectManager.sendMessage(
                    newBoostMessage,
                    contactPubKey,
                    provisionalId.value,
                    MessageType.BOOST,
                    owner.tipAmount?.value ?: 20L,
                    currentChat?.isTribe() ?: false
                )
            }
        }.join()

        return response
    }

    override suspend fun sendTribePayment(
        chatId: ChatId,
        amount: Sat,
        messageUUID: MessageUUID,
        text: String,
    ) {
        applicationScope.launch(mainImmediate) {

            val sendMessageBuilder = SendMessage.Builder()
            sendMessageBuilder.setChatId(chatId)
            sendMessageBuilder.setTribePaymentAmount(amount)
            sendMessageBuilder.setText(text)
            sendMessageBuilder.setReplyUUID(messageUUID.value.toReplyUUID())
            sendMessageBuilder.setIsTribePayment(true)

            sendMessage(
                sendMessageBuilder.build().first
            )
        }
    }

    override fun sendBoost(
        chatId: ChatId,
        boost: FeedBoost
    ) {
        applicationScope.launch(mainImmediate) {
            val message = boost.toJson()

            val sendMessageBuilder = SendMessage.Builder()
            sendMessageBuilder.setChatId(chatId)
            sendMessageBuilder.setText(message)
            sendMessageBuilder.setIsBoost(true)

            sendMessage(
                sendMessageBuilder.build().first
            )
        }
    }

    override suspend fun sendPaymentRequest(requestPayment: SendPaymentRequest): Response<Any, ResponseError> {
        var response: Response<Any, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            val contact: ContactDbo? = requestPayment.contactId?.let {
                withContext(io) {
                    queries.contactGetById(it).executeAsOneOrNull()
                }
            }

            val owner: Contact? = accountOwner.value
                ?: let {
                    // TODO: Handle this better...
                    var owner: Contact? = null
                    try {
                        accountOwner.collect {
                            if (it != null) {
                                owner = it
                                throw Exception()
                            }
                        }
                    } catch (e: Exception) {
                    }
                    delay(25L)
                    owner
                }

            if (owner == null) {
                response = Response.Error(
                    ResponseError("Owner cannot be null")
                )
                return@launch
            }

            var encryptedMemo: MessageContent? = null
            var encryptedRemoteMemo: MessageContent? = null

            requestPayment.memo?.let { msgText ->
                encryptedMemo = owner
                    .rsaPublicKey
                    ?.let { pubKey ->
                        val encResponse = rsa.encrypt(
                            pubKey,
                            UnencryptedString(msgText),
                            formatOutput = false,
                            dispatcher = default,
                        )

                        Exhaustive@
                        when (encResponse) {
                            is Response.Error -> {
                                LOG.e(TAG, encResponse.message, encResponse.exception)
                                null
                            }
                            is Response.Success -> {
                                MessageContent(encResponse.value.value)
                            }
                        }
                    }

                contact?.let { contact ->
                    encryptedRemoteMemo = contact
                        .public_key
                        ?.let { pubKey ->
                            val encResponse = rsa.encrypt(
                                pubKey,
                                UnencryptedString(msgText),
                                formatOutput = false,
                                dispatcher = default,
                            )

                            Exhaustive@
                            when (encResponse) {
                                is Response.Error -> {
                                    LOG.e(TAG, encResponse.message, encResponse.exception)
                                    null
                                }
                                is Response.Success -> {
                                    MessageContent(encResponse.value.value)
                                }
                            }
                        }
                }
            }

            val postRequestPaymentDto = PostPaymentRequestDto(
                requestPayment.chatId?.value,
                requestPayment.contactId?.value,
                requestPayment.amount,
                encryptedMemo?.value,
                encryptedRemoteMemo?.value
            )

            // TODO V2 sendPaymentRequest

//            networkQueryMessage.sendPaymentRequest(postRequestPaymentDto).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        LOG.e(TAG, loadResponse.message, loadResponse.exception)
//                        response = loadResponse
//                    }
//                    is Response.Success -> {
//                        response = Response.Success(true)
//
//                        val message = loadResponse.value
//
//                        decryptMessageDtoContentIfAvailable(
//                            message,
//                            coroutineScope { this },
//                        )
//
//                        chatLock.withLock {
//                            messageLock.withLock {
//                                withContext(io) {
//
//                                    queries.transaction {
//                                        upsertMessage(message, queries)
//
//                                        if (message.updateChatDboLatestMessage) {
//                                            message.chat_id?.toChatId()?.let { chatId ->
//                                                updateChatDboLatestMessage(
//                                                    message,
//                                                    chatId,
//                                                    latestMessageUpdatedTimeMap,
//                                                    queries
//                                                )
//                                            }
//                                        }
//                                    }
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }.join()

        return response ?: Response.Error(ResponseError("Failed to send payment request"))
    }

    override suspend fun payPaymentRequest(message: Message): Response<Any, ResponseError> {
        var response: Response<Any, ResponseError>? = null

        message.paymentRequest?.let { lightningPaymentRequest ->
            applicationScope.launch(mainImmediate) {
                val queries = coreDB.getSphinxDatabaseQueries()

                val putPaymentRequestDto = PutPaymentRequestDto(
                    lightningPaymentRequest.value,
                )

                // TODO V2 payPaymentRequest

//                networkQueryMessage.payPaymentRequest(
//                    putPaymentRequestDto,
//                ).collect { loadResponse ->
//                    Exhaustive@
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//                            response = Response.Error(
//                                ResponseError(loadResponse.message, loadResponse.exception)
//                            )
//                        }
//                        is Response.Success -> {
//                            response = loadResponse
//
//                            val message = loadResponse.value
//
//                            messageLock.withLock {
//                                withContext(io) {
//                                    queries.transaction {
//                                        upsertMessage(message, queries)
//
//                                        if (message.updateChatDboLatestMessage) {
//                                            message.chat_id?.toChatId()?.let { chatId ->
//                                                updateChatDboLatestMessage(
//                                                    message,
//                                                    chatId,
//                                                    latestMessageUpdatedTimeMap,
//                                                    queries
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            }.join()
        }

        return response ?: Response.Error(ResponseError("Failed to pay invoice"))
    }

    override suspend fun payPaymentRequest(
        putPaymentRequestDto: PutPaymentRequestDto
    ): Flow<LoadResponse<Any, ResponseError>>  = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        // TODO V2 payPaymentRequest

//        networkQueryMessage.payPaymentRequest(
//            putPaymentRequestDto,
//        ).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
//                }
//
//                is Response.Error -> {
//                    emit(loadResponse)
//                }
//
//                is Response.Success -> {
//                    emit(loadResponse)
//
//                    val message = loadResponse.value
//
//                    messageLock.withLock {
//                        withContext(io) {
//                            queries.transaction {
//                                upsertMessage(message, queries)
//
//                                if (message.updateChatDboLatestMessage) {
//                                    message.chat_id?.toChatId()?.let { chatId ->
//                                        updateChatDboLatestMessage(
//                                            message,
//                                            chatId,
//                                            latestMessageUpdatedTimeMap,
//                                            queries
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    override suspend fun payAttachment(message: Message) {
        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()

            message.messageMedia?.mediaToken?.let { mediaToken ->
                mediaToken.getPriceFromMediaToken().let { price ->

                    val owner: Contact = accountOwner.value
                        ?: let {
                            // TODO: Handle this better...
                            var owner: Contact? = null
                            try {
                                accountOwner.collect {
                                    if (it != null) {
                                        owner = it
                                        throw Exception()
                                    }
                                }
                            } catch (e: Exception) {}
                            delay(25L)
                            owner
                        } ?: return@launch

                    val contact: Contact? = message.chatId.let { chatId ->
                        getContactById(ContactId(chatId.value)).firstOrNull()
                    }

                    val currentChat = getChatById(message.chatId)

                    val currentProvisionalId: MessageId? = withContext(io) {
                        queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
                    }
                    val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

                    val newPurchase = NewMessage(
                        id = provisionalId,
                        uuid = null,
                        chatId = message.chatId,
                        type = MessageType.Purchase.Processing,
                        sender = owner.id,
                        receiver = null,
                        amount = price,
                        date = DateTime.nowUTC().toDateTime(),
                        expirationDate = null,
                        messageContent = null,
                        status = MessageStatus.Confirmed,
                        seen = Seen.True,
                        senderAlias = null,
                        senderPic = null,
                        originalMUID = message.originalMUID,
                        replyUUID = null,
                        flagged = false.toFlagged(),
                        recipientAlias = null,
                        recipientPic = null,
                        person = null,
                        threadUUID = null,
                        errorMessage = null,
                        messageContentDecrypted = null,
                        messageDecryptionError = false,
                        messageDecryptionException = null,
                        messageMedia = null,
                        feedBoost = null,
                        callLinkMessage = null,
                        podcastClip = null,
                        giphyData = null,
                        reactions = null,
                        purchaseItems = null,
                        replyMessage = null,
                        thread = null
                    )

                    val newPurchaseMessage = chat.sphinx.wrapper.mqtt.Message(
                        null,
                        null,
                        mediaToken.value,
                        null,
                        null,
                        message.uuid?.value,
                        null,
                        null,
                        null
                    ).toJson()


                    chatLock.withLock {
                        messageLock.withLock {
                            withContext(io) {
                                queries.transaction {
                                    upsertNewMessage(newPurchase, queries, null)
                                }
                            }

                            queries.transaction {
                                updateChatNewLatestMessage(
                                    newPurchase,
                                    message.chatId,
                                    latestMessageUpdatedTimeMap,
                                    queries
                                )
                            }
                        }
                    }

                    val contactPubKey = contact?.nodePubKey?.value ?: currentChat?.uuid?.value

                    if (contactPubKey != null) {
                        connectManager.sendMessage(
                            newPurchaseMessage,
                            contactPubKey,
                            provisionalId.value,
                            MessageType.PURCHASE_PROCESSING,
                            price.value,
                            currentChat?.isTribe() ?: false
                        )
                    }
                }
            }
        }
    }

    override suspend fun setNotificationLevel(chat: Chat, level: NotificationLevel): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError> = Response.Success(level.isMuteChat())

        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val contact = queries.contactGetById(ContactId(chat.id.value)).executeAsOneOrNull()

            if (contact != null) {
                contact.node_pub_key?.value?.let { pubKey ->
                    connectManager.setMute(level.value, pubKey)
                }
            } else {
                connectManager.setMute(level.value, chat.uuid.value)
            }

            chatLock.withLock {
                withContext(io) {
                    queries.transaction {
                        updateChatNotificationLevel(
                            chat.id,
                            level,
                            queries
                        )
                    }
                }
            }
        }.join()

        return response
    }

    override suspend fun updateChatContentSeenAt(chatId: ChatId) {
        val queries = coreDB.getSphinxDatabaseQueries()

        chatLock.withLock {
            withContext(io) {
                queries.chatUpdateContentSeenAt(
                    DateTime(DateTimeTz.nowLocal()),
                    chatId
                )
            }
        }
    }


    override suspend fun updateTribeInfo(chat: Chat, isProductionEnvironment: Boolean): NewTribeDto? {
        var owner: Contact? = accountOwner.value

        if (owner == null) {
            try {
                accountOwner.collect {
                    if (it != null) {
                        owner = it
                        throw Exception()
                    }
                }
            } catch (e: Exception) {
            }
            delay(25L)
        }

        var tribeData: NewTribeDto? = null

        chat.host?.let { chatHost ->
            val chatUUID = chat.uuid

            if (chat.isTribe() &&
                chatHost.toString().isNotEmpty() &&
                chatUUID.toString().isNotEmpty()
            ) {
                val queries = coreDB.getSphinxDatabaseQueries()

                networkQueryChat.getTribeInfo(chatHost, LightningNodePubKey(chatUUID.value), isProductionEnvironment)
                    .collect { loadResponse ->
                        when (loadResponse) {

                            is LoadResponse.Loading -> {}
                            is Response.Error -> {}
                            is Response.Success -> {
                                tribeData = loadResponse.value

                                chatLock.withLock {
                                    queries.transaction {
                                        updateNewChatTribeData(loadResponse.value, chat.id, queries)
                                    }
                                }
                            }
                        }
                    }
            }
        }
        return tribeData
    }

    private val podcastLock = Mutex()
    override suspend fun updateFeedContent(
        chatId: ChatId,
        host: ChatHost,
        feedUrl: FeedUrl,
        searchResultDescription: FeedDescription?,
        searchResultImageUrl: PhotoUrl?,
        chatUUID: ChatUUID?,
        subscribed: Subscribed,
        currentItemId: FeedId?
    ) {
        withContext(io) {
            val queries = coreDB.getSphinxDatabaseQueries()

            networkQueryChat.getFeedContent(
                host,
                feedUrl,
                chatUUID
            ).collect { response ->
                Exhaustive@
                when (response) {
                    is LoadResponse.Loading -> {
                    }
                    is Response.Error -> {
                    }
                    is Response.Success -> {

                        var cId: ChatId = chatId

                        response.value.id.toFeedId()?.let { feedId ->
                            queries.feedGetByIds(
                                feedId.youtubeFeedIds()
                            ).executeAsOneOrNull()
                                ?.let { existingFeed ->
                                    //If feed already exists linked to a chat, do not override with NULL CHAT ID
                                    if (chatId.value == ChatId.NULL_CHAT_ID.toLong()) {
                                        cId = existingFeed.chat_id
                                    }
                                }
                        }

                        podcastLock.withLock {
                            queries.transaction {
                                upsertFeed(
                                    response.value,
                                    feedUrl,
                                    searchResultDescription,
                                    searchResultImageUrl,
                                    cId,
                                    currentItemId,
                                    subscribed,
                                    queries
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getFeedByChatId(chatId: ChatId): Flow<Feed?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedGetByChatId(chatId)
            .asFlow()
            .mapToOneOrNull(io)
            .map { it?.let { feedDboPresenterMapper.mapFrom(it) } }
            .distinctUntilChanged()
            .collect { value: Feed? ->
                value?.let { feed ->
                    emit(
                        mapFeedDbo(
                            feed,
                            queries
                        )
                    )
                }
            }
    }

    override fun getFeedById(feedId: FeedId): Flow<Feed?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedGetByIds(feedId.youtubeFeedIds())
            .asFlow()
            .mapToOneOrNull(io)
            .map { it?.let { feedDboPresenterMapper.mapFrom(it) } }
            .distinctUntilChanged()
            .collect { value: Feed? ->
                value?.let { feed ->
                    emit(
                        mapFeedDbo(
                            feed,
                            queries
                        )
                    )
                }
            }
    }

    override fun getFeedItemById(feedItemId: FeedId): Flow<FeedItem?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedItemGetById(feedItemId)
            .asFlow()
            .mapToOneOrNull(io)
            .map { it?.let { feedItemDboPresenterMapper.mapFrom(it) } }
            .distinctUntilChanged()
            .collect { value: FeedItem? ->
                value?.let { feedItem ->
                    emit(feedItem)
                }
            }
    }

    private val feedDboPresenterMapper: FeedDboPresenterMapper by lazy {
        FeedDboPresenterMapper(dispatchers)
    }
    private val feedItemDboPresenterMapper: FeedItemDboPresenterMapper by lazy {
        FeedItemDboPresenterMapper(dispatchers)
    }
    private val feedModelDboPresenterMapper: FeedModelDboPresenterMapper by lazy {
        FeedModelDboPresenterMapper(dispatchers)
    }
    private val feedDestinationDboPresenterMapper: FeedDestinationDboPresenterMapper by lazy {
        FeedDestinationDboPresenterMapper(dispatchers)
    }

    override fun getAllFeedsOfType(feedType: FeedType): Flow<List<Feed>> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        emitAll(
            queries.feedGetAllByFeedType(feedType)
                .asFlow()
                .mapToList(io)
                .map { listFeedDbo ->
                    withContext(default) {
                        mapFeedDboList(
                            listFeedDbo, queries
                        )
                    }
                }
        )
    }

    override fun getAllFeeds(): Flow<List<Feed>> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        emitAll(
            queries.feedGetAllSubscribed()
                .asFlow()
                .mapToList(io)
                .map { listFeedDbo ->
                    withContext(default) {
                        mapFeedDboList(
                            listFeedDbo, queries
                        )
                    }
                }
        )
    }

    private suspend fun mapFeedDboList(
        listFeedDbo: List<FeedDbo>,
        queries: SphinxDatabaseQueries
    ): List<Feed> {
        val itemsMap: MutableMap<FeedId, ArrayList<FeedItem>> =
            LinkedHashMap(listFeedDbo.size)

        val chatsMap: MutableMap<ChatId, Chat?> =
            LinkedHashMap(listFeedDbo.size)

        for (dbo in listFeedDbo) {
            itemsMap[dbo.id] = ArrayList(0)
            chatsMap[dbo.chat_id] = null
        }

        itemsMap.keys.chunked(500).forEach { chunkedIds ->
            queries.feedItemsGetByFeedIds(chunkedIds)
                .executeAsList()
                .let { response ->
                    response.forEach { dbo ->
                        dbo.feed_id?.let { feedId ->
                            itemsMap[feedId]?.add(
                                feedItemDboPresenterMapper.mapFrom(dbo)
                            )
                        }
                    }
                }
        }

        chatsMap.keys.chunked(500).forEach { chunkedChatIds ->
            queries.chatGetAllByIds(chunkedChatIds)
                .executeAsList()
                .let { response ->
                    response.forEach { dbo ->
                        dbo.id?.let { chatId ->
                            chatsMap[chatId] = chatDboPresenterMapper.mapFrom(dbo)
                        }
                    }
                }
        }

        val list = listFeedDbo.map {
            mapFeedDbo(
                feedDbo = it,
                items = itemsMap[it.id] ?: listOf(),
                model = null,
                destinations = listOf(),
                chat = chatsMap[it.chat_id]
            )
        }

        var sortedList: List<Feed>? = null

        withContext(dispatchers.default) {
            sortedList = list.sortedByDescending { it.chat?.contentSeenAt?.time ?: it.lastItem?.datePublished?.time ?: 0 }
        }

        return sortedList ?: listOf()
    }

    private suspend fun mapFeedDbo(
        feedDbo: FeedDbo,
        items: List<FeedItem>,
        model: FeedModel? = null,
        destinations: List<FeedDestination>,
        chat: Chat? = null,
    ): Feed {

        val feed = feedDboPresenterMapper.mapFrom(feedDbo)

        items.forEach { feedItem ->
            feedItem.feed = feed
        }

        feed.items = items
        feed.model = model
        feed.destinations = destinations
        feed.chat = chat

        return feed
    }

    private suspend fun mapFeedDbo(
        feed: Feed,
        queries: SphinxDatabaseQueries
    ): Feed {

        val model = queries.feedModelGetById(feed.id).executeAsOneOrNull()?.let { feedModelDbo ->
            feedModelDboPresenterMapper.mapFrom(feedModelDbo)
        }

        val chat = queries.chatGetById(feed.chatId).executeAsOneOrNull()?.let { chatDbo ->
            chatDboPresenterMapper.mapFrom(chatDbo)
        }

        val items = queries.feedItemsGetByFeedId(feed.id).executeAsList().map {
            feedItemDboPresenterMapper.mapFrom(it)
        }

        val destinations = queries.feedDestinationsGetByFeedId(feed.id).executeAsList().map {
            feedDestinationDboPresenterMapper.mapFrom(it)
        }

        items.forEach { feedItem ->
            feedItem.feed = feed
        }

        feed.items = items
        feed.model = model
        feed.destinations = destinations
        feed.chat = chat

        return feed
    }

    private val podcastDboPresenterMapper: FeedDboPodcastPresenterMapper by lazy {
        FeedDboPodcastPresenterMapper(dispatchers)
    }
    private val podcastDestinationDboPresenterMapper: FeedDestinationDboPodcastDestinationPresenterMapper by lazy {
        FeedDestinationDboPodcastDestinationPresenterMapper(dispatchers)
    }
    private val podcastEpisodeDboPresenterMapper: FeedItemDboPodcastEpisodePresenterMapper by lazy {
        FeedItemDboPodcastEpisodePresenterMapper(dispatchers)
    }
    private val podcastModelDboPresenterMapper: FeedModelDboPodcastModelPresenterMapper by lazy {
        FeedModelDboPodcastModelPresenterMapper(dispatchers)
    }

    override fun getPodcastByChatId(chatId: ChatId): Flow<Podcast?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedGetByChatIdAndType(chatId, FeedType.Podcast)
            .asFlow()
            .mapToOneOrNull(io)
            .map { it?.let { podcastDboPresenterMapper.mapFrom(it) } }
            .distinctUntilChanged()
            .collect { value: Podcast? ->
                value?.let { podcast ->
                    emit(
                        processPodcast(podcast, queries)
                    )
                }
            }
    }

    override fun getPodcastById(feedId: FeedId): Flow<Podcast?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedGetById(feedId)
            .asFlow()
            .mapToOneOrNull(io)
            .map { it?.let { podcastDboPresenterMapper.mapFrom(it) } }
            .distinctUntilChanged()
            .collect { value: Podcast? ->
                value?.let { podcast ->
                    emit(
                        processPodcast(podcast, queries)
                    )
                }
            }
    }

    private suspend fun processPodcast(
        podcast: Podcast,
        queries: SphinxDatabaseQueries
    ): Podcast {

        queries.feedModelGetById(podcast.id).executeAsOneOrNull()?.let { feedModelDbo ->
            podcast.model = podcastModelDboPresenterMapper.mapFrom(feedModelDbo)
        }

        val episodes = queries.feedItemsGetByFeedId(podcast.id).executeAsList().map {
            podcastEpisodeDboPresenterMapper.mapFrom(it)
        }

        val destinations = queries.feedDestinationsGetByFeedId(podcast.id).executeAsList().map {
            podcastDestinationDboPresenterMapper.mapFrom(it)
        }

        podcast.episodes = episodes
        podcast.destinations = destinations

        return podcast
    }

    private val feedSearchResultDboPresenterMapper: FeedDboFeedSearchResultPresenterMapper by lazy {
        FeedDboFeedSearchResultPresenterMapper(dispatchers)
    }

    private suspend fun getSubscribedItemsBy(
        searchTerm: String,
        feedType: FeedType?
    ): MutableList<FeedSearchResultRow> {
        val queries = coreDB.getSphinxDatabaseQueries()
        var results: MutableList<FeedSearchResultRow> = mutableListOf()

        val subscribedItems = if (feedType == null) {
            queries
                .feedGetAllByTitle("%${searchTerm.lowercase().trim()}%")
                .executeAsList()
                .map { it?.let { feedSearchResultDboPresenterMapper.mapFrom(it) } }
        }  else {
            queries
                .feedGetAllByTitleAndType("%${searchTerm.lowercase().trim()}%", feedType)
                .executeAsList()
                .map { it?.let { feedSearchResultDboPresenterMapper.mapFrom(it) } }
        }


        if (subscribedItems.count() > 0) {
            results.add(
                FeedSearchResultRow(
                    feedSearchResult = null,
                    isSectionHeader = true,
                    isFollowingSection = true,
                    isLastOnSection = false
                )
            )

            subscribedItems.forEachIndexed { index, item ->
                results.add(
                    FeedSearchResultRow(
                        item,
                        isSectionHeader = false,
                        isFollowingSection = true,
                        (index == subscribedItems.count() - 1)
                    )
                )
            }
        }

        return results
    }

    override fun searchFeedsBy(
        searchTerm: String,
        feedType: FeedType?,
    ): Flow<List<FeedSearchResultRow>> = flow {
        if (feedType == null) {
            emit(
                getSubscribedItemsBy(searchTerm, feedType)
            )
            return@flow
        }

        var results: MutableList<FeedSearchResultRow> = mutableListOf()

        networkQueryFeedSearch.searchFeeds(
            searchTerm,
            feedType
        ).collect { response ->
            Exhaustive@
            when (response) {
                is LoadResponse.Loading -> {}

                is Response.Error -> {
                    results.addAll(
                        getSubscribedItemsBy(searchTerm, feedType)
                    )
                }
                is Response.Success -> {

                    results.addAll(
                        getSubscribedItemsBy(searchTerm, feedType)
                    )

                    if (response.value.count() > 0) {
                        results.add(
                            FeedSearchResultRow(
                                feedSearchResult = null,
                                isSectionHeader = true,
                                isFollowingSection = false,
                                isLastOnSection = false
                            )
                        )

                        response.value.forEachIndexed { index, item ->
                            results.add(
                                FeedSearchResultRow(
                                    item.toFeedSearchResult(),
                                    isSectionHeader = false,
                                    isFollowingSection = false,
                                    (index == response.value.count() - 1)
                                )
                            )
                        }
                    }
                }
            }
        }

        emit(results)
    }

    override suspend fun toggleFeedSubscribeState(
        feedId: FeedId,
        currentSubscribeState: Subscribed
    ) {
        val queries = coreDB.getSphinxDatabaseQueries()

        queries.feedUpdateSubscribe(
            if (currentSubscribeState.isTrue()) Subscribed.False else Subscribed.True,
            feedId
        )
    }

    private suspend fun mapFeedItemDboList(
        listFeedItemDbo: List<FeedItemDbo>,
        queries: SphinxDatabaseQueries
    ): List<FeedItem> {
        val feedsMap: MutableMap<FeedId, ArrayList<Feed>> =
            LinkedHashMap(listFeedItemDbo.size)

        for (dbo in listFeedItemDbo) {
            feedsMap[dbo.feed_id] = ArrayList(0)
        }

        feedsMap.keys.chunked(500).forEach { chunkedFeedIds ->
            queries.feedGetAllByIds(chunkedFeedIds)
                .executeAsList()
                .let { response ->
                    response.forEach { dbo ->
                        feedsMap[dbo.id]?.add(
                            feedDboPresenterMapper.mapFrom(dbo)
                        )
                    }
                }
        }

        return listFeedItemDbo.map {
            feedItemDboPresenterMapper.mapFrom(it).apply {
                it.feed_id
            }
        }
    }

    /*
* Used to hold in memory the chat table's latest message time to reduce disk IO
* and mitigate conflicting updates between SocketIO and networkRefreshMessages
* */
    @Suppress("RemoveExplicitTypeArguments")
    private val latestMessageUpdatedTimeMap: SynchronizedMap<ChatId, DateTime> by lazy {
        SynchronizedMap<ChatId, DateTime>()
    }

    override val networkRefreshMessages: Flow<LoadResponse<RestoreProgress, ResponseError>> by lazy {
        flow {
            emit(LoadResponse.Loading)
            val queries = coreDB.getSphinxDatabaseQueries()

            val lastSeenMessagesDate: String? = authenticationStorage.getString(
                REPOSITORY_LAST_SEEN_MESSAGE_DATE,
                null
            )

            val page: Int = if (lastSeenMessagesDate == null) {
                authenticationStorage.getString(
                    REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE,
                    "0"
                )!!.toInt()
            } else {
                0
            }

            val lastSeenMessageDateResolved: DateTime = lastSeenMessagesDate?.toDateTime()
                ?: DATE_NIXON_SHOCK.toDateTime()

            val restoring: Boolean = lastSeenMessagesDate == null

            val now: String = DateTime.nowUTC()

            val supervisor = SupervisorJob(currentCoroutineContext().job)
            val scope = CoroutineScope(supervisor)

            var networkResponseError: Response.Error<ResponseError>? = null

            val jobList =
                ArrayList<Job>(MESSAGE_PAGINATION_LIMIT * 2 /* MessageDto fields to potentially decrypt */)

            val latestMessageMap =
                mutableMapOf<ChatId, MessageDto>()

            var offset: Int = page * MESSAGE_PAGINATION_LIMIT
            while (currentCoroutineContext().isActive && offset >= 0) {

                // TODO V2 getMessages

//                networkQueryMessage.getMessages(
//                    MessagePagination.instantiate(
//                        limit = MESSAGE_PAGINATION_LIMIT,
//                        offset = offset,
//                        date = lastSeenMessageDateResolved
//                    )
//                ).collect { response ->
//
//                    Exhaustive@
//                    when (response) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//
//                            offset = -1
//                            networkResponseError = response
//
//                        }
//
//                        is Response.Success -> {
//                            val newMessages = response.value.new_messages
//                            val messagesTotal = response.value.new_messages_total ?: 0
//
//                            if (restoring && messagesTotal > 0) {
//
//                                val restoreProgress = getMessagesRestoreProgress(
//                                    messagesTotal,
//                                    offset
//                                )
//
//                                emit(
//                                    Response.Success(restoreProgress)
//                                )
//                            }
//
//                            if (newMessages.isNotEmpty()) {
//
//                                for (message in newMessages) {
//
//                                    decryptMessageDtoContentIfAvailable(message, scope)
//                                        ?.let { jobList.add(it) }
//
//                                    decryptMessageDtoMediaKeyIfAvailable(message, scope)
//                                        ?.let { jobList.add(it) }
//
//                                }
//
//                                var count = 0
//                                while (currentCoroutineContext().isActive) {
//                                    jobList.elementAtOrNull(count)?.join() ?: break
//                                    count++
//                                }
//
//                                applicationScope.launch(io) {
//
//                                    chatLock.withLock {
//                                        messageLock.withLock {
//
//                                            queries.transaction {
//                                                val chatIds =
//                                                    queries.chatGetAllIds().executeAsList()
//                                                LOG.d(
//                                                    TAG,
//                                                    "Inserting Messages -" +
//                                                            " ${newMessages.firstOrNull()?.id}" +
//                                                            " - ${newMessages.lastOrNull()?.id}"
//                                                )
//
//                                                for (dto in newMessages) {
//
//                                                    val id: Long? = dto.chat_id
//
//                                                    if (id != null &&
//                                                        chatIds.contains(ChatId(id))) {
//
//                                                        if (dto.updateChatDboLatestMessage) {
//                                                            if (!latestMessageMap.containsKey(ChatId(id))) {
//                                                                latestMessageMap[ChatId(id)] = dto
//                                                            } else {
//                                                                val lastMessage = latestMessageMap[ChatId(id)]
//                                                                if (lastMessage == null ||
//                                                                    dto.created_at.toDateTime().time > lastMessage.created_at.toDateTime().time) {
//
//                                                                    latestMessageMap[ChatId(id)] = dto
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//
//                                                    upsertMessage(dto, queries)
//                                                }
//
//                                                latestMessageUpdatedTimeMap.withLock { map ->
//
//                                                    for (entry in latestMessageMap.entries) {
//
//                                                        updateChatDboLatestMessage(
//                                                            entry.value,
//                                                            entry.key,
//                                                            map,
//                                                            queries
//                                                        )
//
//                                                    }
//
//                                                }
//                                            }
//
//                                        }
//                                    }
//                                }.join()
//
//                            }
//
//                            when {
//                                offset == -1 -> {
//                                }
//                                newMessages.size >= MESSAGE_PAGINATION_LIMIT -> {
//                                    offset += MESSAGE_PAGINATION_LIMIT
//
//                                    if (lastSeenMessagesDate == null) {
//                                        val resumePageNumber =
//                                            (offset / MESSAGE_PAGINATION_LIMIT)
//                                        authenticationStorage.putString(
//                                            REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE,
//                                            resumePageNumber.toString()
//                                        )
//                                        LOG.d(
//                                            TAG,
//                                            "Persisting message restore page number: $resumePageNumber"
//                                        )
//                                    }
//
//                                    jobList.clear()
//
//                                }
//                                else -> {
//                                    offset = -1
//                                }
//                            }
//                        }
//                    }
//                }
            }

            supervisor.cancelAndJoin()

            networkResponseError?.let { responseError ->

                emit(responseError)

            } ?: applicationScope.launch(mainImmediate) {

                authenticationStorage.putString(
                    REPOSITORY_LAST_SEEN_MESSAGE_DATE,
                    now
                )

                if (lastSeenMessagesDate == null) {
                    authenticationStorage.removeString(REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE)
                    LOG.d(TAG, "Removing message restore page number")
                }

            }.join()

            emit(Response.Success(
                RestoreProgress(
                    false,
                    100
                )
            ))
        }
    }

    private fun getMessagesRestoreProgress(
        newMessagesTotal: Int,
        offset: Int
    ): RestoreProgress {

        val pages: Int = if (newMessagesTotal <= MESSAGE_PAGINATION_LIMIT) {
            1
        } else {
            newMessagesTotal / MESSAGE_PAGINATION_LIMIT
        }

        val contactsRestoreProgressTotal = 4
        val messagesRestoreProgressTotal = 96
        val currentPage: Int = offset / MESSAGE_PAGINATION_LIMIT
        val progress: Int = contactsRestoreProgressTotal + (currentPage * messagesRestoreProgressTotal / pages)

        return RestoreProgress(
            true,
            progress
        )
    }

    override suspend fun didCancelRestore() {
        val now = DateTime.getFormatRelay().format(
            DateTime.getToday00().value
        )

        authenticationStorage.putString(
            REPOSITORY_LAST_SEEN_MESSAGE_DATE,
            now
        )

        authenticationStorage.removeString(REPOSITORY_LAST_SEEN_MESSAGE_RESTORE_PAGE)
    }

    @OptIn(UnencryptedDataAccess::class)
    private suspend fun decryptMessageDtoContentIfAvailable(
        message: MessageDto,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = mainImmediate
    ): Job? =
        message.message_content?.let { content ->

            if (content.isNotEmpty() && message.type != MessageType.KEY_SEND) {

                scope.launch(dispatcher) {
                    val decrypted = decryptMessageContent(
                        MessageContent(content)
                    )

                    Exhaustive@
                    when (decrypted) {
                        is Response.Error -> {
                            // Only log it if there is an exception
                            decrypted.exception?.let { nnE ->
                                LOG.e(
                                    TAG,
                                    """
                            ${decrypted.message}
                            MessageId: ${message.id}
                            MessageContent: ${message.message_content}
                        """.trimIndent(),
                                    nnE
                                )
                            }
                        }
                        is Response.Success -> {
                            message.setMessageContentDecrypted(
                                decrypted.value.toUnencryptedString(trim = false).value
                            )
                        }
                    }
                }

            } else {
                null
            }
        }

    @OptIn(UnencryptedDataAccess::class)
    private suspend fun decryptMessageDtoMediaKeyIfAvailable(
        message: MessageDto,
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = mainImmediate,
    ): Job? =
        message.media_key?.let { mediaKey ->

            if (mediaKey.isNotEmpty()) {

                scope.launch(dispatcher) {

                    val decrypted = decryptMediaKey(
                        MediaKey(mediaKey)
                    )

                    Exhaustive@
                    when (decrypted) {
                        is Response.Error -> {
                            // Only log it if there is an exception
                            decrypted.exception?.let { nnE ->
                                LOG.e(
                                    TAG,
                                    """
                                    ${decrypted.message}
                                    MessageId: ${message.id}
                                    MediaKey: ${message.media_key}
                                """.trimIndent(),
                                    nnE
                                )
                            }
                        }
                        is Response.Success -> {
                            message.setMediaKeyDecrypted(
                                decrypted.value.toUnencryptedString(trim = false).value
                            )
                        }
                    }

                }

            } else {
                null
            }
        }

    override fun createNewInvite(
        nickname: String,
        welcomeMessage: String
    ): Flow<LoadResponse<Any, ResponseError>> = flow {

        val queries = coreDB.getSphinxDatabaseQueries()

        var response: Response<Any, ResponseError>? = null

        emit(LoadResponse.Loading)

        applicationScope.launch(mainImmediate) {
            // TODO V2 createNewInvite

//            networkQueryContact.createNewInvite(nickname, welcomeMessage)
//                .collect { loadResponse ->
//                    Exhaustive@
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//                            response = loadResponse
//                        }
//
//                        is Response.Success -> {
//                            contactLock.withLock {
//                                withContext(io) {
//                                    queries.transaction {
//                                        updatedContactIds.add(ContactId(loadResponse.value.id))
//                                        upsertContact(loadResponse.value, queries)
//                                    }
//                                }
//                            }
//                            response = Response.Success(true)
//                        }
//                    }
//                }
        }.join()

        emit(response ?: Response.Error(ResponseError("")))
    }

    override suspend fun payForInvite(invite: Invite) {
        val queries = coreDB.getSphinxDatabaseQueries()

        contactLock.withLock {
            withContext(io) {
                queries.transaction {
                    updatedContactIds.add(invite.contactId)
                    updateInviteStatus(invite.id, InviteStatus.ProcessingPayment, queries)
                }
            }
        }

        delay(25L)

        // TODO V2 pay invite

//        networkQueryInvite.payInvite(invite.inviteString).collect { loadResponse ->
//            Exhaustive@
//            when (loadResponse) {
//                is LoadResponse.Loading -> {
//                }
//
//                is Response.Error -> {
//                    contactLock.withLock {
//                        withContext(io) {
//                            queries.transaction {
//                                updatedContactIds.add(invite.contactId)
//                                updateInviteStatus(
//                                    invite.id,
//                                    InviteStatus.PaymentPending,
//                                    queries
//                                )
//                            }
//                        }
//                    }
//                }
//
//                is Response.Success -> {
//                }
//            }
//        }
    }

    override suspend fun deleteInvite(invite: Invite): Response<Any, ResponseError> {
        val queries = coreDB.getSphinxDatabaseQueries()

        val defaultErrorResponse: Response.Error<ResponseError> = Response.Error(
            ResponseError("Failed to delete invite", Exception("Default exception"))
        )

        // TODO V2 deleteContact

//        val response = networkQueryContact.deleteContact(invite.contactId)

//        contactLock.withLock {
//            withContext(io) {
//                queries.transaction {
//                    updatedContactIds.add(invite.contactId)
//                    deleteContactById(invite.contactId, queries)
//                }
//            }
//
//        }
        return defaultErrorResponse
    }

    override suspend fun authorizeExternal(
        relayUrl: String,
        host: String,
        challenge: String
    ): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            // TODO V2 verifyExternal

//            networkQueryAuthorizeExternal.verifyExternal().collect { loadResponse ->
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//
//                    is Response.Error -> {
//                        response = loadResponse
//                    }
//
//                    is Response.Success -> {
//
//                        val token = loadResponse.value.token
//                        val info = loadResponse.value.info

                        // TODO V2 signBase64

//                        networkQueryAuthorizeExternal.signBase64(
//                            AUTHORIZE_EXTERNAL_BASE_64
//                        ).collect { sigResponse ->
//
//                            when (sigResponse) {
//                                is LoadResponse.Loading -> {
//                                }
//
//                                is Response.Error -> {
//                                    response = sigResponse
//                                }
//
//                                is Response.Success -> {
//
//                                    info.verificationSignature = sigResponse.value.sig
//                                    info.url = relayUrl
//
//                                    networkQueryAuthorizeExternal.authorizeExternal(
//                                        host,
//                                        challenge,
//                                        token,
//                                        info,
//                                    ).collect { authorizeResponse ->
//                                        when (authorizeResponse) {
//                                            is LoadResponse.Loading -> {
//                                            }
//
//                                            is Response.Error -> {
//                                                response = authorizeResponse
//                                            }
//
//                                            is Response.Success -> {
//                                                response = Response.Success(true)
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }.join()

        return response ?: Response.Error(ResponseError("Returned before completing"))
    }

    override suspend fun deletePeopleProfile(
        body: String
    ): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            SphinxJson.decodeFromString<DeletePeopleProfileDto>(
                body
            ).let { deletePeopleProfileDto ->
                // TODO V2 deletePeopleProfile
//                networkQuerySaveProfile.deletePeopleProfile(
//                    deletePeopleProfileDto
//                ).collect { loadResponse ->
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//                        is Response.Error -> {
//                        }
//                        is Response.Success -> {
//                            response = Response.Success(true)
//                        }
//                    }
//                }
            }
        }.join()

        return response ?: Response.Error(ResponseError("Profile delete failed"))
    }

    override suspend fun savePeopleProfile(
        body: String
    ): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            SphinxJson.decodeFromString<PeopleProfileDto>(body)?.let { profile ->
                // TODO V2 savePeopleProfile
//                networkQuerySaveProfile.savePeopleProfile(
//                    profile
//                ).collect { saveProfileResponse ->
//                    when (saveProfileResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//                            response = saveProfileResponse
//                        }
//
//                        is Response.Success -> {
//                            response = Response.Success(true)
//                        }
//                    }
//                }
            }
        }.join()

        return response ?: Response.Error(ResponseError("Profile save failed"))
    }

    override suspend fun redeemBadgeToken(
        body: String
    ): Response<Boolean, ResponseError> {
        var response: Response<Boolean, ResponseError>? = null

        applicationScope.launch(mainImmediate) {
            SphinxJson.decodeFromString<RedeemBadgeTokenDto>(body)?.let { profile ->
                // TODO V2 redeemBadgeToken
//                networkQueryRedeemBadgeToken.redeemBadgeToken(
//                    profile
//                ).collect { redeemBadgeTokenResponse ->
//                    when (redeemBadgeTokenResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//                            response = redeemBadgeTokenResponse
//                        }
//
//                        is Response.Success -> {
//                            response = Response.Success(true)
//                        }
//                    }
//                }
            }
        }.join()

        return response ?: Response.Error(ResponseError("Redeem Badge Token failed"))
    }



    override suspend fun exitAndDeleteTribe(tribe: Chat) {
        val queries = coreDB.getSphinxDatabaseQueries()
        applicationScope.launch(io) {

            val currentProvisionalId: MessageId? = withContext(io) {
                queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
            }
            val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)
            val isDeleteTribe = tribe.isTribeOwnedByAccount(accountOwner.value?.nodePubKey)
            val messageType = if(isDeleteTribe) MessageType.TRIBE_DELETE else MessageType.GROUP_LEAVE

            val newMessage = chat.sphinx.wrapper.mqtt.Message(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            ).toJson()

            tribe.uuid.value.let { pubKey ->
                deleteAllMessagesAndPubKey(pubKey, tribe.id)

                connectManager.sendMessage(
                    newMessage,
                    pubKey,
                    provisionalId.value,
                    messageType,
                    null,
                    true
                )
            }

            chatLock.withLock {
                messageLock.withLock {
                    withContext(io) {
                        queries.transaction {
                            deleteChatById(
                                tribe.id,
                                queries,
                                latestMessageUpdatedTimeMap
                            )
                        }
                    }
                }
            }
        }.join()
    }

    override suspend fun storeTribe(createTribe: CreateTribe, chatId: ChatId?) {
        val memeServerHost = MediaHost.DEFAULT

        applicationScope.launch(mainImmediate) {
            try {
                val imgUrl: String? = createTribe.img?.toFile()?.let { imgFile ->
                    // If an image file is provided we should upload it
                    val token =
                        memeServerTokenHandler.retrieveAuthenticationToken(memeServerHost)
                            ?: throw RuntimeException("MemeServerAuthenticationToken retrieval failure")

                    val networkResponse = networkQueryMemeServer.uploadAttachment(
                        authenticationToken = token,
                        mediaType = MediaType.Image("${MediaType.IMAGE}/${imgFile.extension}"),
                        path = imgFile.toOkioPath(),
                        fileName = imgFile.name,
                        contentLength = imgFile.length(),
                        memeServerHost = memeServerHost,
                    )
                    Exhaustive@
                    when (networkResponse) {
                        is Response.Error -> {
                            LOG.e(TAG, "Failed to upload image: ", networkResponse.exception)
                            null
                        }
                        is Response.Success -> {
                            "https://${memeServerHost.value}/public/${networkResponse.value.muid}"
                        }
                    }
                }

                val ownerAlias = accountOwner.value?.alias?.value ?: "unknown"

                if (chatId == null) {
                    val newTribeJson = createTribe.toNewCreateTribe(ownerAlias, imgUrl, null).toJson()
                    connectManager.createTribe(newTribeJson)
                } else {
                    val tribe = getChatById(chatId)
                    if (tribe != null) {
                        val updatedTribeJson = createTribe.toNewCreateTribe(ownerAlias, imgUrl, tribe.uuid.value).toJson()
                        tribe.ownerPubKey?.value?.let { connectManager.editTribe(updatedTribeJson) }
                    }
                }

            } catch (e: Exception) { }
        }
    }

    override suspend fun updateTribe(
        chatId: ChatId,
        createTribe: CreateTribe
    ): Response<Any, ResponseError> {

        var response: Response<Any, ResponseError> =
            Response.Error(ResponseError(("Failed to exit tribe")))
        val memeServerHost = MediaHost.DEFAULT

        applicationScope.launch(mainImmediate) {
            try {
                val imgUrl: String? = (createTribe.img?.toFile()?.let { imgFile ->
                    val token =
                        memeServerTokenHandler.retrieveAuthenticationToken(memeServerHost)
                            ?: throw RuntimeException("MemeServerAuthenticationToken retrieval failure")

                    val networkResponse = networkQueryMemeServer.uploadAttachment(
                        authenticationToken = token,
                        mediaType = MediaType.Image("${MediaType.IMAGE}/${imgFile.extension}"),
                        path = imgFile.toOkioPath(),
                        fileName = imgFile.name,
                        contentLength = imgFile.length(),
                        memeServerHost = memeServerHost,
                    )
                    Exhaustive@
                    when (networkResponse) {
                        is Response.Error -> {
                            LOG.e(TAG, "Failed to upload image: ", networkResponse.exception)
                            response = networkResponse
                            null
                        }
                        is Response.Success -> {
                            "https://${memeServerHost.value}/public/${networkResponse.value.muid}"
                        }
                    }
                }) ?: createTribe.imgUrl


                // TODO V2 updateTribe

//                networkQueryChat.updateTribe(
//                    chatId,
//                    createTribe.toPostGroupDto(imgUrl)
//                ).collect { loadResponse ->
//                    when (loadResponse) {
//                        is LoadResponse.Loading -> {
//                        }
//
//                        is Response.Error -> {
//                            response = loadResponse
//                            LOG.e(TAG, "Failed to create tribe: ", loadResponse.exception)
//                        }
//                        is Response.Success -> {
//                            response = Response.Success(loadResponse.value)
//                            val queries = coreDB.getSphinxDatabaseQueries()
//
//                            var owner: Contact? = accountOwner.value
//
//                            if (owner == null) {
//                                try {
//                                    accountOwner.collect {
//                                        if (it != null) {
//                                            owner = it
//                                            throw Exception()
//                                        }
//                                    }
//                                } catch (e: Exception) {
//                                }
//                                delay(25L)
//                            }
//
//                            chatLock.withLock {
//                                messageLock.withLock {
//                                    withContext(io) {
//                                        queries.transaction {
//                                            upsertChat(
//                                                loadResponse.value,
//                                                chatSeenMap,
//                                                queries,
//                                                null,
//                                                owner?.nodePubKey
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            } catch (e: Exception) {
                response = Response.Error(
                    ResponseError("Failed to update Chat Profile", e)
                )
            }
        }.join()

        return response
    }

    override fun processMemberRequest(
        chatId: ChatId,
        messageUuid: MessageUUID?,
        memberPubKey: LightningNodePubKey?,
        type: MessageType.GroupAction,
        alias: SenderAlias?,
    ) {
        val messageBuilder = SendMessage.Builder()
        messageBuilder.setChatId(chatId)
        messageBuilder.setGroupAction(type)

        // Accept or Reject member
        messageUuid?.value?.let { nnMessageUuid ->
            messageBuilder.setReplyUUID(ReplyUUID(nnMessageUuid))
        }

        // Kick Member
        memberPubKey?.let { nnContactKey ->
            messageBuilder.setMemberPubKey(nnContactKey)
        }

        alias?.let { senderAlias ->
            messageBuilder.setSenderAlias(senderAlias)
        }

        sendMessage(messageBuilder.build().first)
    }

    /***
     * Subscriptions
     */

    private val subscriptionLock = Mutex()
    private val subscriptionDboPresenterMapper: SubscriptionDboPresenterMapper by lazy {
        SubscriptionDboPresenterMapper(dispatchers)
    }

    override fun getActiveSubscriptionByContactId(contactId: ContactId): Flow<Subscription?> =
        flow {
            emitAll(
                coreDB.getSphinxDatabaseQueries()
                    .subscriptionGetLastActiveByContactId(contactId)
                    .asFlow()
                    .mapToOneOrNull(io)
                    .map { it?.let { subscriptionDboPresenterMapper.mapFrom(it) } }
                    .distinctUntilChanged()
            )
        }

    override suspend fun createSubscription(
        amount: Sat,
        interval: String,
        contactId: ContactId,
        chatId: ChatId?,
        endDate: String?,
        endNumber: EndNumber?
    ): Response<Any, ResponseError> {
        var response: Response<SubscriptionDto, ResponseError>? = null

        // TODO V2 postSubscription

        return response ?: Response.Error(ResponseError(("Failed to create subscription")))
    }

    override suspend fun updateSubscription(
        id: SubscriptionId,
        amount: Sat,
        interval: String,
        contactId: ContactId,
        chatId: ChatId?,
        endDate: String?,
        endNumber: EndNumber?
    ): Response<Any, ResponseError> {
        var response: Response<SubscriptionDto, ResponseError>? = null

        applicationScope.launch(mainImmediate) {

            // TODO V2 putSubscription

//            networkQuerySubscription.putSubscription(
//                id,
//                PutSubscriptionDto(
//                    amount = amount.value,
//                    contact_id = contactId.value,
//                    chat_id = chatId?.value,
//                    interval = interval,
//                    end_number = endNumber?.value,
//                    end_date = endDate
//                )
//            ).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        response = loadResponse
//                    }
//                    is Response.Success -> {
//                        response = loadResponse
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        subscriptionLock.withLock {
//                            withContext(io) {
//                                queries.transaction {
//                                    upsertSubscription(
//                                        loadResponse.value,
//                                        queries
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }.join()

        return response ?: Response.Error(ResponseError(("Failed to update subscription")))
    }

    override suspend fun restartSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError> {
        var response: Response<SubscriptionDto, ResponseError>? = null

        applicationScope.launch(mainImmediate) {

            // TODO V2 putRestartSubscription

//            networkQuerySubscription.putRestartSubscription(
//                subscriptionId
//            ).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        response = loadResponse
//                    }
//                    is Response.Success -> {
//                        response = loadResponse
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        subscriptionLock.withLock {
//                            withContext(io) {
//                                queries.transaction {
//                                    upsertSubscription(
//                                        loadResponse.value,
//                                        queries
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }.join()

        return response ?: Response.Error(ResponseError(("Failed to restart subscription")))
    }

    override suspend fun pauseSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError> {
        var response: Response<SubscriptionDto, ResponseError>? = null

        applicationScope.launch(mainImmediate) {

            // TODO V2 putPauseSubscription

//            networkQuerySubscription.putPauseSubscription(
//                subscriptionId
//            ).collect { loadResponse ->
//                Exhaustive@
//                when (loadResponse) {
//                    is LoadResponse.Loading -> {
//                    }
//                    is Response.Error -> {
//                        response = loadResponse
//                    }
//                    is Response.Success -> {
//                        response = loadResponse
//                        val queries = coreDB.getSphinxDatabaseQueries()
//
//                        subscriptionLock.withLock {
//                            withContext(io) {
//                                queries.transaction {
//                                    upsertSubscription(
//                                        loadResponse.value,
//                                        queries
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }.join()

        return response ?: Response.Error(ResponseError(("Failed to pause subscription")))
    }

    override suspend fun deleteSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError> {
        var response: Response<Any, ResponseError>? = null

        // TODO V2 deleteSubscription

        return response ?: Response.Error(ResponseError(("Failed to delete subscription")))
    }

    private val downloadMessageMediaLockMap = SynchronizedMap<MessageId, Pair<Int, Mutex>>()
    override fun downloadMediaIfApplicable(
        message: Message,
        sent: Boolean
    ) {
        applicationScope.launch(mainImmediate) {
            val messageId: MessageId = message.id

            val downloadLock: Mutex = downloadMessageMediaLockMap.withLock { map ->
                val localLock: Pair<Int, Mutex>? = map[messageId]

                if (localLock != null) {
                    map[messageId] = Pair(localLock.first + 1, localLock.second)
                    localLock.second
                } else {
                    Pair(1, Mutex()).let { pair ->
                        map[messageId] = pair
                        pair.second
                    }
                }
            }

            downloadLock.withLock {
                val queries = coreDB.getSphinxDatabaseQueries()

                //Getting media data from purchase accepted item if is paid content

                val urlAndMedia = message?.retrieveUrlAndMessageMedia()
                val media = urlAndMedia?.second
                val host = media?.host
                val url = urlAndMedia?.first?.toMediaUrlOrNull() ?: media?.url

                val localFile = message?.messageMedia?.localFile

                if (
                    message != null &&
                    media != null &&
                    host != null &&
                    url != null &&
                    localFile == null &&
                    !message.status.isDeleted() &&
                    (!message.isPaidPendingMessage || sent)
                ) {
                    memeServerTokenHandler.retrieveAuthenticationToken(host)?.let { token ->
                        memeInputStreamHandler.retrieveMediaInputStream(
                            url.value,
                            token,
                            media.mediaKeyDecrypted,
                        )?.let { streamAndFileName ->

                            mediaCacheHandler.createFile(
                                mediaType = message.messageMedia?.mediaType ?: media.mediaType,
                                extension = streamAndFileName.second?.getExtension()
                            )?.let { streamToFile ->

                                streamAndFileName.first?.let { stream ->
                                    mediaCacheHandler.copyTo(stream, streamToFile)
                                    messageLock.withLock {
                                        withContext(io) {
                                            queries.transaction {

                                                queries.messageMediaUpdateFile(
                                                    streamToFile,
                                                    streamAndFileName.second,
                                                    messageId
                                                )

                                                // to proc table change so new file path is pushed to UI
                                                queries.messageUpdateContentDecrypted(
                                                    message.messageContentDecrypted,
                                                    messageId
                                                )
                                            }
                                        }
                                    }
                                }
                            } ?: run {
                                if (message.messageMedia?.mediaType?.isSphinxText == true) {
                                    streamAndFileName?.first?.bufferedReader().use { it?.readText() }?.toMessageContentDecrypted()?.let {
                                        queries.messageUpdateContentDecrypted(
                                            it,
                                            messageId
                                        )
                                    }
                                }
                            }
                            delay(200L)
                        }
                    }
                }

                // remove lock from map if only subscriber
                downloadMessageMediaLockMap.withLock { map ->
                    map[messageId]?.let { pair ->
                        if (pair.first <= 1) {
                            map.remove(messageId)
                        } else {
                            map[messageId] = Pair(pair.first - 1, pair.second)
                        }
                    }
                }
            }
        }
    }

    private val feedItemLock = Mutex()
    private val downloadFeedItemLockMap = SynchronizedMap<FeedId, Pair<Int, Mutex>>()

    override fun inProgressDownloadIds(): List<FeedId> {
        return downloadFeedItemLockMap.withLock { map ->
            map.keys.toList()
        }
    }

    override fun downloadMediaIfApplicable(
        feedItem: DownloadableFeedItem,
        downloadCompleteCallback: (downloadedFilePath: Path) -> Unit
    ) {
        val feedItemId: FeedId = feedItem.id

        val downloadLock: Mutex = downloadFeedItemLockMap.withLock { map ->
            val localLock: Pair<Int, Mutex>? = map[feedItemId]

            if (localLock != null) {
                map[feedItemId] = Pair(localLock.first + 1, localLock.second)
                localLock.second
            } else {
                Pair(1, Mutex()).let { pair ->
                    map[feedItemId] = pair
                    pair.second
                }
            }
        }

        applicationScope.launch(mainImmediate) {
            downloadLock.withLock {
                sphinxNotificationManager.notify(
                    notificationId = SphinxNotificationManager.DOWNLOAD_NOTIFICATION_ID,
                    title = "Downloading Item",
                    message = "Downloading item for local playback",
                )

                val queries = coreDB.getSphinxDatabaseQueries()

                val url = feedItem.enclosureUrl.value
                val contentType = feedItem.enclosureType
                val localFile = feedItem.localFile

                if (
                    contentType != null &&
                    localFile == null
                ) {
                    val streamToFilePath: Path? = mediaCacheHandler.createFile(
                        contentType.value.toMediaType()
                    )

                    if (streamToFilePath != null) {
                        memeInputStreamHandler.retrieveMediaInputStream(
                            url,
                            authenticationToken = null,
                            mediaKeyDecrypted = null,
                        )?.let { streamAndFileName ->
                            streamAndFileName.first?.let { stream ->
                                sphinxNotificationManager.notify(
                                    notificationId = SphinxNotificationManager.DOWNLOAD_NOTIFICATION_ID,
                                    title = "Completing Download",
                                    message = "Finishing up download of file",
                                )
                                mediaCacheHandler.copyTo(stream, streamToFilePath)

                                feedItemLock.withLock {
                                    withContext(io) {
                                        queries.transaction {
                                            queries.feedItemUpdateLocalFile(
                                                streamToFilePath,
                                                feedItemId
                                            )
                                        }
                                    }
                                }

                                sphinxNotificationManager.notify(
                                    notificationId = SphinxNotificationManager.DOWNLOAD_NOTIFICATION_ID,
                                    title = "Download complete",
                                    message = "item can now be accessed offline",
                                )
                                // hold downloadLock until table change propagates to UI
                                delay(200L)
                                downloadCompleteCallback.invoke(streamToFilePath)
                            } ?: FileSystem.SYSTEM.delete(streamToFilePath)
                        } ?: FileSystem.SYSTEM.delete(streamToFilePath)
                    }
                } else {
                    val title = if (localFile != null) {
                        "Item already downloaded"
                    } else {
                        "Failed to initiate download"
                    }
                    val message = if (localFile != null) {
                        "You have already downloaded this item."
                    } else {
                        "Failed to initiate download because of missing media type information"
                    }
                    sphinxNotificationManager.notify(
                        notificationId = SphinxNotificationManager.DOWNLOAD_NOTIFICATION_ID,
                        title = title,
                        message = message,
                    )
                }

                // remove lock from map if only subscriber
                downloadFeedItemLockMap.withLock { map ->
                    map[feedItemId]?.let { pair ->
                        if (pair.first <= 1) {
                            map.remove(feedItemId)
                        } else {
                            map[feedItemId] = Pair(pair.first - 1, pair.second)
                        }
                    }
                }
            }
        }
    }


    override suspend fun deleteDownloadedMediaIfApplicable(
        feedItem: DownloadableFeedItem
    ): Boolean {
        val feedItemId: FeedId = feedItem.id
        val queries = coreDB.getSphinxDatabaseQueries()

        val localFile = feedItem.localFile

        localFile?.let {
            try {
                if (FileSystem.SYSTEM.exists(it)) {
                    FileSystem.SYSTEM.delete(it)
                }

                feedItemLock.withLock {
                    withContext(io) {
                        queries.transaction {
                            queries.feedItemUpdateLocalFile(
                                null,
                                feedItemId
                            )
                        }
                    }
                }
                delay(200L)

                return true
            } catch (e: Exception) {

            }
        }
        return false
    }

    override suspend fun getPaymentTemplates(): Response<List<PaymentTemplate>, ResponseError> {
        var response: Response<List<PaymentTemplate>, ResponseError>? = null

        val memeServerHost = MediaHost.DEFAULT

        memeServerTokenHandler.retrieveAuthenticationToken(memeServerHost)?.let { token ->
            networkQueryMemeServer.getPaymentTemplates(token)
                .collect { loadResponse ->
                    Exhaustive@
                    when (loadResponse) {
                        is LoadResponse.Loading -> {
                        }

                        is Response.Error -> {
                            response = loadResponse
                        }

                        is Response.Success -> {
                            var templates = ArrayList<PaymentTemplate>(loadResponse.value.size)

                            for (ptDto in loadResponse.value) {
                                templates.add(
                                    PaymentTemplate(
                                        ptDto.muid,
                                        ptDto.width,
                                        ptDto.height,
                                        token.value
                                    )
                                )
                            }

                            response = Response.Success(templates)
                        }
                    }
                }
        }

        return response ?: Response.Error(ResponseError(("Failed to load payment templates")))
    }

    override suspend fun getAllMessagesToShowByChatIdPaginated(chatId: ChatId): Flow<PagingData<Message>> {
        val queries = coreDB.getSphinxDatabaseQueries()

        return Pager(
            config = PagingConfig(
                pageSize = 21,
                enablePlaceholders = false,
                maxSize = 1000
            )
        ) {
            QueryPagingSource(
                countQuery = queries.messageCountAllToShowByChatId(chatId),
                transacter = queries,
                dispatcher = io,
                queryProvider = { limit: Long, offset: Long ->
                    queries.messageGetAllToShowByChatIdPaginated(chatId, limit, offset)
                }
            )
        }.flow.map { pagingData: PagingData<MessageDbo> ->
            pagingData.map { messageDbo: MessageDbo ->
                mapMessageDboAndDecryptContentIfNeeded(
                    queries = queries,
                    messageDbo = messageDbo,
                    reactions = emptyList(), // TODO: load reactions messageDbo.uuid?.let { reactionsMap[it] },
                    thread = null,
                    purchaseItems = emptyList(), // TODO: load purchaseItems messageDbo.muid?.let { purchaseItemsMap[it] },
                    replyMessage = messageDbo.reply_uuid,
                )
            }
        }
        // TODO: Give it a scope it is cached in...
    }

    override suspend fun messageMediaUpdateLocalFile(message: Message, filepath: Path) {
        val queries = coreDB.getSphinxDatabaseQueries()

        messageLock.withLock {
            withContext(io) {
                queries.transaction {
                    queries.messageMediaUpdateFile(
                        filepath,
                        null,
                        message.id
                    )
                }
            }
        }
    }

    override suspend fun upsertMqttMessage(
        msg: Msg,
        msgSender: MsgSender,
        contactTribePubKey: String,
        msgType: MessageType,
        msgUuid: MessageUUID,
        msgIndex: MessageId,
        msgAmount: Sat?,
        originalUuid: MessageUUID?,
        timestamp: DateTime?,
        date: DateTime?,
        fromMe: Boolean,
        realPaymentAmount: Sat?,
        paymentRequest: LightningPaymentRequest?,
        paymentHash: LightningPaymentHash?,
        bolt11: Bolt11?,
        tag: TagMessage?
    ) {
        val queries = coreDB.getSphinxDatabaseQueries()
        val contact = contactTribePubKey.toLightningNodePubKey()?.let { getContactByPubKey(it).firstOrNull() }
        val owner = accountOwner.value
        val chatTribe = contactTribePubKey.toChatUUID()?.let { getChatByUUID(it).firstOrNull() }
        var messageMedia: MessageMediaDbo? = null
        val isTribe = contact == null

        if (contact != null || chatTribe != null) {

            originalUuid?.let { uuid ->
                queries.messageUpdateUUIDByUUID(msgUuid, uuid )
            }

            // On Conversation ChatId is contactId defined by the bindings,
            // tribes use the auto-generated chatId
            val chatId = when {
                contact?.id?.value != null -> contact.id.value
                chatTribe?.id?.value != null -> chatTribe.id.value
                else -> 0L
            }

            val existingMessage = queries.messageGetByUUID(msgUuid).executeAsOneOrNull()
            val messageId = existingMessage?.id

            if (fromMe && messageId?.value != null && messageId.value < 0) {
                val existingMessageMedia = messageId.let {
                    queries.messageMediaGetById(it).executeAsOneOrNull()
                }?.copy(id = msgIndex)

                existingMessageMedia?.let {
                    messageLock.withLock {
                        queries.messageMediaDeleteById(messageId)
                    }
                    messageMedia = existingMessageMedia
                }
            } else {
                msg.mediaToken?.toMediaToken()?.let { mediaToken ->

                    messageMedia = MessageMediaDbo(
                        msgIndex,
                        ChatId(chatId),
                        msg.mediaKey?.toMediaKey(),
                        msg.mediaKey?.toMediaKeyDecrypted(),
                        msg.mediaType?.toMediaType() ?: MediaType.Unknown(""),
                        mediaToken,
                        null,
                        null
                    )
                }
            }

            messageLock.withLock {
                queries.messageDeleteByUUID(msgUuid)
            }

            val senderAlias = msgSender.alias?.toSenderAlias()

            val hasPaymentRequest = paymentRequest != null || existingMessage?.payment_request != null

            val status = when {
                fromMe && hasPaymentRequest -> MessageStatus.Pending
                fromMe && !hasPaymentRequest -> MessageStatus.Confirmed
                !fromMe && hasPaymentRequest -> MessageStatus.Pending
                else -> MessageStatus.Received
            }

            val isTribeBoost = isTribe && msgType is MessageType.Boost
            val amount = if (fromMe || isTribeBoost) msgAmount else realPaymentAmount

            val now = DateTime.nowUTC().toDateTime()
            val messageDate = if (isTribe) date ?: now else timestamp ?: now

            val newMessage = NewMessage(
                id = msgIndex,
                uuid = msgUuid,
                chatId = ChatId(chatId),
                type = msgType,
                sender = if (fromMe) ContactId(0) else contact?.id ?: ContactId(chatId) ,
                receiver = ContactId(0),
                amount = bolt11?.getSatsAmount() ?: existingMessage?.amount ?: amount ?: Sat(0L),
                paymentRequest = existingMessage?.payment_request ?: paymentRequest,
                paymentHash = existingMessage?.payment_hash ?: msg.paymentHash?.toLightningPaymentHash() ?: paymentHash,
                date = messageDate,
                expirationDate = existingMessage?.expiration_date ?: bolt11?.getExpiryTime()?.toDateTime(),
                messageContent = null,
                status = status,
                seen = Seen.False,
                senderAlias = senderAlias,
                senderPic = msgSender.photo_url?.toPhotoUrl(),
                originalMUID = null,
                replyUUID = existingMessage?.reply_uuid ?: msg.replyUuid?.toReplyUUID(),
                flagged = Flagged.False,
                recipientAlias = null,
                recipientPic = null,
                person = null,
                threadUUID = existingMessage?.thread_uuid ?: msg.threadUuid?.toThreadUUID(),
                errorMessage = null,
                tagMessage = existingMessage?.tag_message ?: tag,
                messageContentDecrypted = if (msg.content?.isNotEmpty() == true) MessageContentDecrypted(msg.content!!) else null,
                messageDecryptionError = false,
                messageDecryptionException = null,
                messageMedia = messageMedia?.let { MessageMediaDboWrapper(it) },
                feedBoost = null,
                callLinkMessage = null,
                podcastClip = null,
                giphyData = null,
                reactions = null,
                purchaseItems = null,
                replyMessage = null,
                thread = null
            )
            contact?.id?.let { contactId ->
                if (!fromMe) {
                    val lastMessageIndex = getLastMessage().firstOrNull()?.id?.value
                    val newMessageIndex = msgIndex.value

                    if (lastMessageIndex != null) {
                        if (lastMessageIndex < newMessageIndex) {
                            contactLock.withLock {
                                msgSender.photo_url?.takeIf { it.isNotEmpty() && it != contact.photoUrl?.value }?.let {
                                    queries.contactUpdatePhotoUrl(it.toPhotoUrl(), contactId)
                                }
                                msgSender.alias?.takeIf { it.isNotEmpty() && it != contact.alias?.value }?.let {
                                    queries.contactUpdateAlias(it.toContactAlias(), contactId)
                                }
                            }
                        }
                    }
                } else {
                    contactLock.withLock {
                        if (owner != null) {
                            if (owner.alias?.value == null) {
                                msgSender.alias?.takeIf { it.isNotEmpty() && it != owner.alias?.value }
                                    ?.let {
                                        queries.contactUpdateAlias(it.toContactAlias(), owner.id)

                                        connectManager.ownerInfoStateFlow.value?.let { ownerInfo ->
                                            connectManager.setOwnerInfo(
                                                ownerInfo.copy(alias = it)
                                            )
                                        }
                                    }
                            }

                            if (owner.photoUrl?.value == null) {
                                msgSender.photo_url?.takeIf { it.isNotEmpty() && it != owner.photoUrl?.value }
                                    ?.let {
                                        queries.contactUpdatePhotoUrl(it.toPhotoUrl(), owner.id)

                                        connectManager.ownerInfoStateFlow.value?.let { ownerInfo ->
                                            connectManager.setOwnerInfo(
                                                ownerInfo.copy(picture = it)
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            if (msgType is MessageType.Payment) {
                queries.messageUpdateInvoiceAsPaidByPaymentHash(
                    msg.paymentHash?.toLightningPaymentHash()
                )
            }

            messageLock.withLock {
                queries.transaction {
                    upsertNewMessage(newMessage, queries, messageMedia?.file_name)

                    updateChatNewLatestMessage(
                        newMessage,
                        ChatId(chatId),
                        latestMessageUpdatedTimeMap,
                        queries
                    )
                }
            }

            chatLock.withLock {
                queries.chatUpdateSeen(Seen.False, ChatId(chatId))
            }
        }
    }

    private suspend fun upsertGenericPaymentMsg(
        msg: Msg,
        msgType: MessageType,
        msgIndex: MessageId,
        msgAmount: Sat?,
        timestamp: DateTime?,
        paymentHash: LightningPaymentHash?,
    ) {
        val queries = coreDB.getSphinxDatabaseQueries()

        val newMessage = NewMessage(
            id = msgIndex,
            chatId = ChatId(ChatId.NULL_CHAT_ID.toLong()),
            type = msgType,
            sender = ContactId(-1),
            receiver = ContactId(0),
            amount = msgAmount ?: Sat(0L),
            paymentHash = paymentHash,
            date = timestamp ?: DateTime.nowUTC().toDateTime(),
            status = MessageStatus.Received,
            seen = Seen.False,
            flagged = Flagged.False,
            messageContentDecrypted = if (msg.content?.isNotEmpty() == true) MessageContentDecrypted(msg.content) else null,
            messageDecryptionError = false,
        )

        messageLock.withLock {
            queries.transaction {
                upsertNewMessage(newMessage, queries, null)
            }
        }
    }


    override suspend fun deleteMqttMessage(messageUuid: MessageUUID) {
        val queries = coreDB.getSphinxDatabaseQueries()

        messageLock.withLock {
            queries.messageUpdateStatusByUUID(MessageStatus.Deleted, messageUuid)
        }
    }


    override fun getMaxIdMessage(): Flow<Long?> = flow {
        emitAll(
            coreDB.getSphinxDatabaseQueries()
                .messageGetMaxId()
                .asFlow()
                .mapToOneOrNull(io)
                .map { it?.MAX }
        )
    }

    override fun getLastMessage(): Flow<Message?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        emitAll(
            queries.messageGetLastMessage()
                .asFlow()
                .mapToOneOrNull(io)
                .map {
                    it?.let { messageDbo ->
                        mapMessageDboAndDecryptContentIfNeeded(queries, messageDbo)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override fun getTribeLastMemberRequestBySenderAlias(alias: SenderAlias, chatId: ChatId): Flow<Message?> = flow {
        val queries = coreDB.getSphinxDatabaseQueries()
        emitAll(
            queries.messageLastMemberRequestGetBySenderAlias(alias, chatId)
                .asFlow()
                .mapToOneOrNull(io)
                .map {
                    it?.let { messageDbo ->
                        mapMessageDboAndDecryptContentIfNeeded(queries, messageDbo)
                    }
                }
                .distinctUntilChanged()
        )
    }

    override fun sendMediaKeyOnPaidPurchase(
        msg: Msg,
        contactInfo: MsgSender,
        paidAmount: Sat
    ) {
        applicationScope.launch {
            val queries = coreDB.getSphinxDatabaseQueries()
            val muid = msg.mediaToken?.toMediaToken()?.getMUIDFromMediaToken()

            muid?.value?.let { nnMuid ->
                val message = queries.messageGetByMuid(MessageMUID(nnMuid)).executeAsOneOrNull()
                val mediaMessage = message?.id?.let { queries.messageMediaGetById(it) }?.executeAsOneOrNull()

                val contact: Contact? = message?.chat_id?.let { chatId ->
                    getContactById(ContactId(chatId.value)).firstOrNull()
                }

                val messageType = if (message?.amount?.value == paidAmount.value) {
                    MessageType.Purchase.Accepted
                } else {
                    MessageType.Purchase.Denied
                }

                if (message != null && contact != null && mediaMessage != null) {

                    val currentProvisionalId: MessageId? = withContext(io) {
                        queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
                    }
                    val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

                    // Message Accepted
                    val mediaMessageAccepted = mediaMessage.copy(media_key = null, media_key_decrypted = null, local_file = null)
                    val messageAccepted = message.copy(
                        type = messageType,
                        id = provisionalId
                    ).let { convertMessageDboToNewMessage(it, mediaMessageAccepted) }

                    chatLock.withLock {
                        messageLock.withLock {
                            withContext(io) {

                                queries.transaction {
                                    upsertNewMessage(messageAccepted, queries, null)
                                }

                                queries.transaction {
                                    updateChatNewLatestMessage(
                                        messageAccepted,
                                        message.chat_id,
                                        latestMessageUpdatedTimeMap,
                                        queries
                                    )
                                }
                            }
                        }
                    }

                    val mediaKey = if (messageType is MessageType.Purchase.Accepted) {
                        mediaMessage.media_key?.value
                    }
                    else {
                        null
                    }

                    val newPurchaseMessage = chat.sphinx.wrapper.mqtt.Message(
                        null,
                        null,
                        mediaMessage.media_token.value,
                        mediaKey,
                        null,
                        null,
                        null,
                        null,
                        null
                    ).toJson()

                    contact.let { nnContact ->
                        connectManager.sendMessage(
                            newPurchaseMessage,
                            contact.nodePubKey?.value ?: "",
                            provisionalId.value,
                            messageType.value,
                            null,
                        )
                    }
                }
            }
        }
    }

    override suspend fun sendNewPaymentRequest(requestPayment: SendPayment) {
        applicationScope.launch(mainImmediate) {
            val queries = coreDB.getSphinxDatabaseQueries()
            val chatId = requestPayment.chatId ?: return@launch
            val contact = requestPayment.contactId?.value?.let { ContactId(it) }
                ?.let { getContactById(it).firstOrNull() }

            val currentProvisionalId: MessageId? = withContext(io) {
                queries.messageGetLowestProvisionalMessageId().executeAsOneOrNull()
            }
            val provisionalId = MessageId((currentProvisionalId?.value ?: 0L) - 1)

            val invoiceAndHash = connectManager.createInvoice(requestPayment.amount, requestPayment.text ?: "")

            val newPaymentRequest = NewMessage(
                id = provisionalId,
                uuid = null,
                chatId = chatId,
                type = MessageType.Invoice,
                sender = accountOwner.value?.id ?: ContactId(0),
                receiver = null,
                amount = requestPayment.amount.toSat() ?: Sat(0),
                paymentHash = invoiceAndHash?.second?.toLightningPaymentHash(),
                paymentRequest = invoiceAndHash?.first?.toLightningPaymentRequestOrNull(),
                date = DateTime.nowUTC().toDateTime(),
                expirationDate = null,
                messageContent = null,
                status = MessageStatus.Pending,
                seen = Seen.True,
                senderAlias = accountOwner.value?.alias?.value?.toSenderAlias(),
                senderPic = accountOwner.value?.photoUrl,
                originalMUID = null,
                replyUUID = null,
                flagged = false.toFlagged(),
                recipientAlias = null,
                recipientPic = null,
                person = null,
                threadUUID = null,
                errorMessage = null,
                messageContentDecrypted = requestPayment.text?.toMessageContentDecrypted(),
                messageDecryptionError = false,
                messageDecryptionException = null,
                messageMedia = null,
                feedBoost = null,
                callLinkMessage = null,
                podcastClip = null,
                giphyData = null,
                reactions = null,
                purchaseItems = null,
                replyMessage = null,
                thread = null
            )

            val newPaymentRequestMessage = chat.sphinx.wrapper.mqtt.Message(
                requestPayment.text,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                invoiceAndHash?.first
            ).toJson()

            chatLock.withLock {
                messageLock.withLock {
                    withContext(io) {
                        queries.transaction {
                            upsertNewMessage(newPaymentRequest, queries, null)
                        }
                    }

                    queries.transaction {
                        updateChatNewLatestMessage(
                            newPaymentRequest,
                            chatId,
                            latestMessageUpdatedTimeMap,
                            queries
                        )
                    }
                }
            }

            if (contact != null) {
                connectManager.sendMessage(
                    newPaymentRequestMessage,
                    contact.nodePubKey?.value ?: "",
                    provisionalId.value,
                    MessageType.INVOICE,
                    null,
                    false
                )
            }
        }
    }

    suspend fun getOwner() : Contact? {
        var owner: Contact? = accountOwner.value

        if (owner == null) {
            try {
                accountOwner.collect {
                    if (it != null) {
                        owner = it
                        throw Exception()
                    }
                }
            } catch (e: Exception) {
            }
            delay(25L)
        }

        return owner
    }
}
