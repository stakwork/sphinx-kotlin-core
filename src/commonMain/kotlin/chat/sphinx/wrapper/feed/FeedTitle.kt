package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedTitle(): FeedTitle? =
    try {
        FeedTitle(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedTitle(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "FeedTitle cannot be empty"
        }
    }
}