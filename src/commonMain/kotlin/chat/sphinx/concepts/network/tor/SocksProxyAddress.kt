package chat.sphinx.concepts.network.tor

import kotlin.jvm.JvmInline

inline val SocksProxyAddress.host: String
    get() = value.split(':')[0]

inline val SocksProxyAddress.port: Int
    get() = value.split(':')[1].toInt()

@JvmInline
value class SocksProxyAddress(val value: String)
