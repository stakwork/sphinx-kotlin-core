package chat.sphinx.concepts.network.tor

import io.matthewnelson.kmp.tor.manager.common.event.TorManagerEvent


inline fun TorManagerEvent.AddressInfo.toSocksProxyAddress(): SocksProxyAddress? {
    return splitSocks().getOrNull()?.firstOrNull()?.let {
        SocksProxyAddress(it.address, it.port.value)
    }
}

class SocksProxyAddress(
    val host: String,
    val port: Int
)
