package chat.sphinx.wrapper.message

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessageUUID(): MessageUUID? =
    try {
        MessageUUID(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPinnedMessageUUID(): MessageUUID =
    try {
        MessageUUID(this)
    } catch (e: IllegalArgumentException) {
        MessageUUID("_")
    }

@JvmInline
value class MessageUUID(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "MessageUUID cannot be empty"
        }
    }
}
