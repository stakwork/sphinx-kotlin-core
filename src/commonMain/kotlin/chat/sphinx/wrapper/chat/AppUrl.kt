package chat.sphinx.wrapper.chat

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toAppUrl(): AppUrl? =
    try {
        AppUrl(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class AppUrl(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "AppUrl cannot be empty"
        }
    }
}
