package chat.sphinx.wrapper.chat

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toChatUUID(): ChatUUID? =
    try {
        ChatUUID(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class ChatUUID(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "ChatUUID cannot be empty"
        }
    }
}
