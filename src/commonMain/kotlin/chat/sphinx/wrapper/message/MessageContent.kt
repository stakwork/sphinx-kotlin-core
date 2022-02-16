package chat.sphinx.wrapper.message

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessageContent(): MessageContent? =
    try {
        MessageContent(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class MessageContent(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "MessageContent cannot be empty"
        }
    }
}
