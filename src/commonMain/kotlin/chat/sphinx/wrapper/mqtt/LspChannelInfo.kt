package chat.sphinx.wrapper.mqtt

import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.ShortChannelId
import chat.sphinx.wrapper.lightning.toLightningNodePubKey
import chat.sphinx.wrapper.lightning.toShortChannelId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun String.toLspChannelInfo(): LspChannelInfo? {
    return try {
        Json.decodeFromString<LspChannelInfoKMP>(this)?.let {
            LspChannelInfo(
                it.scid.toShortChannelId(),
                it.serverPubkey.toLightningNodePubKey()
            )
        }
    } catch (e: Exception) {
        null
    }
}

data class LspChannelInfo(
    val scid: ShortChannelId?,
    val serverPubKey: LightningNodePubKey?
)

@Serializable
data class LspChannelInfoKMP(
    val scid: String,
    val serverPubkey: String
)
