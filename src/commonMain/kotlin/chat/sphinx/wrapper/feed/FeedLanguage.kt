package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedLanguage(): FeedLanguage? =
    try {
        FeedLanguage(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedLanguage(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "FeedLanguage cannot be empty"
        }
    }
}