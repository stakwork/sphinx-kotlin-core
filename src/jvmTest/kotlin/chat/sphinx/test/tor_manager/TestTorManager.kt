package chat.sphinx.test.tor_manager

import chat.sphinx.concepts.network.tor.SocksProxyAddress
import io.matthewnelson.kmp.tor.common.address.OnionAddress
import io.matthewnelson.kmp.tor.common.address.OnionAddressV3
import io.matthewnelson.kmp.tor.common.clientauth.ClientName
import io.matthewnelson.kmp.tor.common.clientauth.OnionClientAuth
import io.matthewnelson.kmp.tor.controller.common.config.ClientAuthEntry
import io.matthewnelson.kmp.tor.controller.common.config.ConfigEntry
import io.matthewnelson.kmp.tor.controller.common.config.HiddenServiceEntry
import io.matthewnelson.kmp.tor.controller.common.config.TorConfig
import io.matthewnelson.kmp.tor.controller.common.control.TorControlOnionClientAuth
import io.matthewnelson.kmp.tor.controller.common.control.usecase.TorControlInfoGet
import io.matthewnelson.kmp.tor.controller.common.control.usecase.TorControlOnionAdd
import io.matthewnelson.kmp.tor.controller.common.control.usecase.TorControlSignal
import io.matthewnelson.kmp.tor.controller.common.events.TorEvent
import io.matthewnelson.kmp.tor.manager.TorManager
import io.matthewnelson.kmp.tor.manager.common.event.TorManagerEvent
import io.matthewnelson.kmp.tor.manager.common.state.TorNetworkState
import io.matthewnelson.kmp.tor.manager.common.state.TorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestTorManager(override val isDestroyed: Boolean = false) : TorManager {
    val socksProxyAddressStateFlow: StateFlow<SocksProxyAddress?>
        get() = MutableStateFlow(null)

    suspend fun getSocksPortSetting(): String {
        return TorConfig.Setting.Ports.Socks().default.value
    }

    override val state: TorState
        get() = TorState.Off

    override val networkState: TorNetworkState
        get() = TorNetworkState.Disabled

    override suspend fun start(): Result<Any?> {
        return Result.success(true)
    }

    override fun startQuietly() {

    }

    override suspend fun stop(): Result<Any?> {
        return Result.success(true)
    }

    override fun stopQuietly() {

    }

    override suspend fun restart(): Result<Any?> {
        return Result.success(true)
    }

    override fun restartQuietly() {

    }

    override suspend fun setEvents(events: Set<TorEvent>, extended: Boolean): Result<Any?> {
        return Result.success(true)
    }

    override suspend fun signal(signal: TorControlSignal.Signal): Result<Any?> {
        return Result.success(true)
    }

//    override fun newIdentity() {}

    private var torIsRequired: Boolean? = null
    suspend fun setTorRequired(required: Boolean) {
        torIsRequired = required
    }

    suspend fun isTorRequired(): Boolean? {
        return torIsRequired
    }

    override fun addListener(listener: TorManagerEvent.SealedListener): Boolean {
        return false
    }

    override suspend fun configGet(setting: TorConfig.Setting<*>): Result<ConfigEntry> {
        return Result.success(ConfigEntry("test"))
    }

    override suspend fun configGet(settings: Set<TorConfig.Setting<*>>): Result<List<ConfigEntry>> {
        TODO("Not yet implemented")
    }

    override suspend fun configLoad(config: TorConfig): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun configReset(setting: TorConfig.Setting<*>, setDefault: Boolean): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun configReset(settings: Set<TorConfig.Setting<*>>, setDefault: Boolean): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun configSave(force: Boolean): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun configSet(setting: TorConfig.Setting<*>): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun configSet(settings: Set<TorConfig.Setting<*>>): Result<Any?> {
        TODO("Not yet implemented")
    }

    override fun debug(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun destroy(stopCleanly: Boolean, onCompletion: (() -> Unit)?) {
        TODO("Not yet implemented")
    }

    override suspend fun dropGuards(): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun infoGet(keyword: TorControlInfoGet.KeyWord): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun infoGet(keywords: Set<TorControlInfoGet.KeyWord>): Result<Map<String, String>> {
        TODO("Not yet implemented")
    }

    override suspend fun onionAdd(
        privateKey: OnionAddress.PrivateKey,
        hsPorts: Set<TorConfig.Setting.HiddenService.Ports>,
        flags: Set<TorControlOnionAdd.Flag>?,
        maxStreams: TorConfig.Setting.HiddenService.MaxStreams?
    ): Result<HiddenServiceEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun onionAddNew(
        type: OnionAddress.PrivateKey.Type,
        hsPorts: Set<TorConfig.Setting.HiddenService.Ports>,
        flags: Set<TorControlOnionAdd.Flag>?,
        maxStreams: TorConfig.Setting.HiddenService.MaxStreams?
    ): Result<HiddenServiceEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun onionClientAuthAdd(
        address: OnionAddressV3,
        key: OnionClientAuth.PrivateKey,
        clientName: ClientName?,
        flags: Set<TorControlOnionClientAuth.Flag>?
    ): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun onionClientAuthRemove(address: OnionAddressV3): Result<Any?> {
        TODO("Not yet implemented")
    }

    override suspend fun onionClientAuthView(): Result<List<ClientAuthEntry>> {
        TODO("Not yet implemented")
    }

    override suspend fun onionClientAuthView(address: OnionAddressV3): Result<ClientAuthEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun onionDel(address: OnionAddress): Result<Any?> {
        TODO("Not yet implemented")
    }


    override fun removeListener(listener: TorManagerEvent.SealedListener): Boolean {
        return false
    }
}