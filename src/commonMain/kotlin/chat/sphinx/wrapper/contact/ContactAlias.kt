package chat.sphinx.wrapper.contact

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toContactAlias(): ContactAlias? =
    try {
        ContactAlias(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class ContactAlias(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "ContactAlias cannot be empty"
        }
    }
}
