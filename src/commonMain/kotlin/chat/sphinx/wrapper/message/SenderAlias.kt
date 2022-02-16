package chat.sphinx.wrapper.message

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toSenderAlias(): SenderAlias? =
    try {
        SenderAlias(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class SenderAlias(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "SenderAlias cannot be empty"
        }
    }
}
