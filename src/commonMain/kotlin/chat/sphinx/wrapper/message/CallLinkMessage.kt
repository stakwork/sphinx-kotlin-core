package chat.sphinx.wrapper.message

import chat.sphinx.utils.SphinxJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("NOTHING_TO_INLINE")
inline fun String.toCallLinkMessageOrNull(): CallLinkMessage? =
    try {
        this.replaceFirst(CallLinkMessage.MESSAGE_PREFIX, "").toCallLinkMessage()
    } catch (e: Exception) {
        null
    }

fun String.toCallLinkMessage(): CallLinkMessage =
    SphinxJson.decodeFromString<CallLinkMessageMoshi>(this).let {
        CallLinkMessage(
            SphinxCallLink(it.link),
            it.recurring,
            it.cron
        )
    }

@Throws(AssertionError::class)
fun CallLinkMessage.toJson(): String =
    Json.encodeToString(
        CallLinkMessageMoshi(
            link.value,
            recurring,
            cron
        )
    )

data class CallLinkMessage(
    val link: SphinxCallLink,
    val recurring: Boolean,
    val cron: String,
) {
    companion object {
        const val MESSAGE_PREFIX = "call::"
    }
}

@Serializable
internal data class CallLinkMessageMoshi(
    val link: String,
    val recurring: Boolean,
    val cron: String
)