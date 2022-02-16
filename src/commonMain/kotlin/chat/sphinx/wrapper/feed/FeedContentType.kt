package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedContentType(): FeedContentType? =
    try {
        FeedContentType(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedContentType(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "FeedContentType cannot be empty"
        }
    }
}