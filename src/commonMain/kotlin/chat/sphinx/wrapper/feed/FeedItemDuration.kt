package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toFeedItemDuration(): FeedItemDuration? =
    try {
        FeedItemDuration(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedItemDuration(val value: Long) {
    init {
        require(value >= 0) {
            "FeedItemDuration must be greater than or equal to 0"
        }
    }
}