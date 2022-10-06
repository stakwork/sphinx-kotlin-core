package chat.sphinx.features.network.client

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.network.client.NetworkClientClearedListener
import chat.sphinx.concepts.network.client.cache.NetworkClientCache
import chat.sphinx.concepts.network.tor.SocksProxyAddress
import chat.sphinx.concepts.network.tor.toSocksProxyAddress
import chat.sphinx.di.container.SphinxContainer
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.utils.build_config.BuildConfigDebug
import chat.sphinx.wrapper.relay.isOnionAddress
import io.matthewnelson.kmp.tor.controller.common.config.TorConfig
import io.matthewnelson.kmp.tor.controller.common.events.TorEvent
import io.matthewnelson.kmp.tor.manager.TorManager
import io.matthewnelson.kmp.tor.manager.common.TorControlManager
import io.matthewnelson.kmp.tor.manager.common.TorOperationManager
import io.matthewnelson.kmp.tor.manager.common.event.TorManagerEvent
import io.matthewnelson.kmp.tor.manager.common.state.TorNetworkState
import io.matthewnelson.kmp.tor.manager.common.state.TorState
import io.matthewnelson.kmp.tor.manager.common.state.isOff
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.jvm.Volatile



class NetworkClientImpl(
    private val applicationScope: CoroutineScope,
    private val debug: BuildConfigDebug,
    private val cache: Cache,
    private val dispatchers: CoroutineDispatchers,
    private val authenticationStorage: AuthenticationStorage,
    redactedLoggingHeaders: RedactedLoggingHeaders?,
    private val torManager: TorManager,
    private val LOG: SphinxLogger,
) : NetworkClientCache(),
    CoroutineDispatchers by dispatchers
{
    // only expose necessary interfaces
    val torOperationManager: TorOperationManager get() = torManager
    val torControlManager: TorControlManager get() = torManager

    private val listener = TorManagerListener()
    val eventLines: StateFlow<String> get() = listener.eventLines
    val addressInfo: StateFlow<TorManagerEvent.AddressInfo> get() = listener.addressInfo
    val state: StateFlow<TorManagerEvent.State> get() = listener.state

    private val lock = Mutex()
    private val requirementChangeLock = Mutex()

    override suspend fun setTorRequired(required: Boolean) {
        lock.withLock {
            val requiredString = if (required) TRUE else FALSE

            when (isTorRequiredCache) {
                null -> {

                    applicationScope.launch(mainImmediate) {
                        requirementChangeLock.withLock {
                            val persisted = authenticationStorage.getString(TOR_MANAGER_REQUIRED, null)

                            isTorRequiredCache = if (persisted != requiredString) {
                                authenticationStorage.putString(TOR_MANAGER_REQUIRED, requiredString)
                                required
                            } else {
                                required
                            }
                        }
                    }.join()

                }
                required -> {
                    // no change, do nothing
                }
                else -> {
                    applicationScope.launch(mainImmediate) {
                        requirementChangeLock.withLock {
                            authenticationStorage.putString(TOR_MANAGER_REQUIRED, requiredString)
                            isTorRequiredCache = required
                        }
                    }.join()
                }
            }
        }
    }

    override suspend fun isTorRequired(): Boolean {
        var required: Boolean?

        lock.withLock {
            requirementChangeLock.withLock {
                required = isTorRequiredCache ?: authenticationStorage.getString(TOR_MANAGER_REQUIRED, null)?.let { persisted ->
                    when (persisted) {
                        null -> {
                            null
                        }
                        TRUE -> {
                            isTorRequiredCache = true
                            true
                        }
                        else -> {
                            isTorRequiredCache = false
                            false
                        }
                    }
                }
            }
        }

        return required ?: run {
            val relayUrl = SphinxContainer.networkModule.relayDataHandlerImpl.retrieveRelayUrl()
            val required = relayUrl?.isOnionAddress == true
            setTorRequired(required)
            return required
        }
    }

    companion object {
        const val TAG = "NetworkClientImpl"

        const val TIME_OUT = 15L
        const val PING_INTERVAL = 25L

        const val CACHE_CONTROL = "Cache-Control"
        const val MAX_STALE = "public, max-stale=$MAX_STALE_VALUE"

        // PersistentStorage keys
        const val TOR_MANAGER_REQUIRED = "TOR_MANAGER_REQUIRED"

        @Volatile
        private var isTorRequiredCache: Boolean? = null
        const val TRUE = "T"
        const val FALSE = "F"
    }

    /////////////////
    /// Listeners ///
    /////////////////
    private inner class SynchronizedListenerHolder {
        private val lock = SynchronizedObject()
        private val listeners: LinkedHashSet<NetworkClientClearedListener> = LinkedHashSet(0)

        fun addListener(listener: NetworkClientClearedListener): Boolean {
            synchronized(lock) {
                val bool = listeners.add(listener)
                if (bool) {
                    LOG.d(TAG, "Listener ${listener::class.simpleName} registered")
                }
                return bool
            }
        }

        fun removeListener(listener: NetworkClientClearedListener): Boolean {
            synchronized(lock) {
                val bool = listeners.remove(listener)
                if (bool) {
                    LOG.d(TAG, "Listener ${listener::class.simpleName} removed")
                }
                return bool
            }
        }

        fun clear() {
            synchronized(lock) {
                if (listeners.isNotEmpty()) {
                    listeners.clear()
                    LOG.d(TAG, "Listeners cleared")
                }
            }
        }

        fun dispatchClearedEvent() {
            synchronized(lock) {
                for (listener in listeners) {
                    listener.networkClientCleared()
                }
            }
        }

        val hasListeners: Boolean
            get() = synchronized(lock) {
                listeners.isNotEmpty()
            }
    }

    private val synchronizedListeners = SynchronizedListenerHolder()

    override fun addListener(listener: NetworkClientClearedListener): Boolean {
        return synchronizedListeners.addListener(listener)
    }

    override fun removeListener(listener: NetworkClientClearedListener): Boolean {
        return synchronizedListeners.removeListener(listener)
    }

    ///////////////
    /// Clients ///
    ///////////////
    class RedactedLoggingHeaders(val headers: List<String>)

    @Volatile
    private var client: OkHttpClient? = null
    @Volatile
    private var clearedClient: OkHttpClient? = null

    private val clientLock = Mutex()
    private var currentClientSocksProxyAddress: SocksProxyAddress? = null

    private val cryptoInterceptor: CryptoInterceptor by lazy {
        CryptoInterceptor()
    }

    private val loggingInterceptor: HttpLoggingInterceptor? by lazy {
        if (debug.value) {
            HttpLoggingInterceptor().let { interceptor ->
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                redactedLoggingHeaders?.headers?.let { list ->
                    for (header in list) {
                        if (header.isNotEmpty()) {
                            interceptor.redactHeader(header)
                        }
                    }
                }
                interceptor
            }
        } else {
            null
        }
    }

    override suspend fun getClient(): OkHttpClient =
        clientLock.withLock {
            client ?: (clearedClient?.newBuilder()?.also { clearedClient = null } ?: OkHttpClient.Builder()).apply {
                connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                readTimeout(TIME_OUT, TimeUnit.SECONDS)
                writeTimeout(TIME_OUT, TimeUnit.SECONDS)

                if (isTorRequired()) {

                    torManager.start()

                    var socksPortJob: Job? = null
                    var torStateJob: Job? = null

                    coroutineScope {
                        socksPortJob = launch(mainImmediate) {
                            try {
                                // wait for Tor to start and publish its socks address after
                                // being bootstrapped.
                                addressInfo.collect { addressInfo ->
                                    addressInfo.toSocksProxyAddress()?.let {
                                        proxy(
                                            Proxy(
                                                Proxy.Type.SOCKS,
                                                InetSocketAddress(it.host, it.port)
                                            )
                                        )
                                        currentClientSocksProxyAddress = it

                                        throw Exception()
                                    }
                                }
                            } catch (e: Exception) {}
                        }

                        torStateJob = launch(mainImmediate) {
                            var retry: Int = 3
                            delay(250L)
                            try {
                                state.collect {
                                    if (it.isOff) {
                                        if (retry >= 0) {
                                            LOG.d(TAG, "Tor failed to start, retrying: $retry")
                                            torManager.start()
                                            retry--
                                        } else {
                                            socksPortJob?.cancel()
                                            throw Exception()
                                        }
                                    }

                                    if (it.isOn) {
                                        throw Exception()
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }

                    torStateJob?.join()
                    socksPortJob?.join()

                    // Tor failed to start, but we still want to set the proxy port
                    // so we don't leak _any_ network requests.
                    if (
                        currentClientSocksProxyAddress == null && torManager.state is TorState.Off
                    ) {

                        val socksPort: Int = addressInfo.value.toSocksProxyAddress()?.port ?: TorConfig.Setting.Ports.Socks().default.value.toInt()

                        proxy(
                            Proxy(
                                Proxy.Type.SOCKS,
                                InetSocketAddress("127.0.0.1", socksPort)
                            )
                        )
                        currentClientSocksProxyAddress = SocksProxyAddress("127.0.0.1", socksPort)
                    }

                    // check again in case the setting has changed
                    if (isTorRequired()) {
                        proxy(null)
                        currentClientSocksProxyAddress = null

                        torManager.stopQuietly()

                        LOG.d(
                            TAG,
                            """
                                Tor requirement changed to false while building the network client.
                                Proxy settings removed and stopTor called.
                            """.trimIndent()
                        )
                    } else {
                        LOG.d(TAG, "Client built with $currentClientSocksProxyAddress")
                    }
                } else {
                    currentClientSocksProxyAddress = null
                    proxy(null)
                }

                if (!interceptors().contains(cryptoInterceptor)) {
                    addInterceptor(cryptoInterceptor)
                }

                loggingInterceptor?.let { nnInterceptor ->
                    if (!networkInterceptors().contains(nnInterceptor)) {
                        addNetworkInterceptor(nnInterceptor)
                    }
                }

            }
                .build()
                .also { client = it }
        }

    @Volatile
    private var cachingClient: OkHttpClient? = null
    private val cachingClientLock = Mutex()

    override suspend fun getCachingClient(): OkHttpClient =
        cachingClientLock.withLock {
            cachingClient ?: getClient().newBuilder()
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header(CACHE_CONTROL, MAX_STALE)
                        .build()
                    chain.proceed(request)
                }
                .build()
                .also { cachingClient = it }
        }

    //////////////////////////
    /// TorManagerListener ///
    //////////////////////////
    // TODO: Handle this changing...
    suspend fun onTorRequirementChange(required: Boolean) {
        cachingClientLock.withLock {
            clientLock.withLock {
                client?.let { nnClient ->
                    if (required) {
                        if (currentClientSocksProxyAddress == null) {
                            clearedClient = nnClient
                            client = null
                            cachingClient = null
                            synchronizedListeners.dispatchClearedEvent()
                        }
                    } else {
                        if (currentClientSocksProxyAddress != null) {
                            clearedClient = nnClient
                            client = null
                            cachingClient = null
                            synchronizedListeners.dispatchClearedEvent()
                        }
                    }
                }
            }
        }
    }

    suspend fun onTorSocksProxyAddressChange(socksProxyAddress: SocksProxyAddress?) {
        if (socksProxyAddress == null) {
            return
        }

        cachingClientLock.withLock {
            clientLock.withLock {
                client?.let { nnClient ->
                    // We don't want to close down the client,
                    // just move it temporarily so it forces a rebuild
                    // with the new proxy settings
                    clearedClient = nnClient
                    client = null
                    cachingClient = null
                    synchronizedListeners.dispatchClearedEvent()
                }
            }
        }
    }

    init {
        startTor()
    }

    fun startTor() {
        torManager.debug(true)
        torManager.addListener(listener)

        // TODO: Move to SampleView along with stop/restart buttons
        torManager.startQuietly()
    }

    fun stopTor() {
        // just in case setupOnCloseIntercept fails.
        torManager.destroy(stopCleanly = false) {
            // will not be invoked if TorManager has already been destroyed
//            Log.w(this.javaClass.simpleName, "onCloseRequest intercept failed. Tor did not stop cleanly.")
        }
    }

    /**
     * Must call [TorManager.destroy] to stop Tor and clean up so that the
     * Application does not hang on exit.
     *
     * See [stop] also.
     * */
    private fun onClose() {
        // `destroy` launches a coroutine using TorManager's scope in order
        // to stop Tor cleanly via it's control port. This takes ~500ms if Tor
        // is running.
        //
        // Upon destruction completion, Platform.exit() will be invoked.
        torManager.destroy(stopCleanly = true) {
            // onCompletion
//            Platform.exit()
        }
    }

    private class TorManagerListener: TorManagerEvent.Listener() {
        private val _eventLines: MutableStateFlow<String> = MutableStateFlow("")
        val eventLines: StateFlow<String> = _eventLines.asStateFlow()
        private val events: MutableList<String> = ArrayList(50)

        fun addLine(line: String) {
            synchronized(this) {
                if (events.size > 49) {
                    events.removeAt(0)
                }
                events.add(line)
                // TODO: Log the line....
                _eventLines.value = events.joinToString("\n")
            }
        }

        private val _addressInfo: MutableStateFlow<TorManagerEvent.AddressInfo> =
            MutableStateFlow(TorManagerEvent.AddressInfo())
        val addressInfo: StateFlow<TorManagerEvent.AddressInfo> = _addressInfo.asStateFlow()
        override fun managerEventAddressInfo(info: TorManagerEvent.AddressInfo) {
            _addressInfo.value = info
        }

        private val _state: MutableStateFlow<TorManagerEvent.State> =
            MutableStateFlow(TorManagerEvent.State(TorState.Off, TorNetworkState.Disabled))
        val state: StateFlow<TorManagerEvent.State> = _state.asStateFlow()

        override fun managerEventState(state: TorManagerEvent.State) {
            _state.value = state
        }

        override fun onEvent(event: TorManagerEvent) {
            addLine(event.toString())
            if (event is TorManagerEvent.Log.Error) {
                event.value.printStackTrace()
            }
            when (event) {
                is TorManagerEvent.Log.Error -> {

                }
                TorManagerEvent.Action.Controller -> {

                }
                TorManagerEvent.Action.Restart -> {

                }
                TorManagerEvent.Action.Start -> {

                }
                TorManagerEvent.Action.Stop -> {

                }
                is TorManagerEvent.Log.Debug -> {

                }
                is TorManagerEvent.Log.Info -> {

                }
                is TorManagerEvent.Log.Warn -> {

                }
                is TorManagerEvent.Lifecycle<*> -> {

                }
                is TorManagerEvent.AddressInfo -> {
                    event.splitSocks()


                }
                is TorManagerEvent.State -> {

                }
            }

            super.onEvent(event)
        }

        override fun onEvent(event: TorEvent.Type.SingleLineEvent, output: String) {
            addLine("event=${event.javaClass.simpleName}, output=$output")
        }

        override fun onEvent(event: TorEvent.Type.MultiLineEvent, output: List<String>) {
            addLine("multi-line event: ${event.javaClass.simpleName}. See Logs.")

            // these events are many many many lines and should be moved
            // off the main thread if ever needed to be dealt with.
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
//                Log.d("SampleListener", "-------------- multi-line event START: ${event.javaClass.simpleName} --------------")
//                for (line in output) {
//                    Log.d("SampleListener", line)
//                }
//                Log.d("SampleListener", "--------------- multi-line event END: ${event.javaClass.simpleName} ---------------")
            }
        }
    }
}
