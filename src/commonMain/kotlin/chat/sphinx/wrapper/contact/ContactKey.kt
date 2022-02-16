package chat.sphinx.wrapper.contact

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toContactKey(): ContactKey? =
    try {
        ContactKey(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class ContactKey(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "ContactKey cannot be empty"
        }
    }
}
