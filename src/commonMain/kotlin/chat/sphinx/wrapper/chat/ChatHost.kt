package chat.sphinx.wrapper.chat

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toChatHost(): ChatHost? =
    try {
        ChatHost(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class ChatHost(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "ChatHost cannot be empty"
        }
    }
}
