package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toFeedItemsCount(): FeedItemsCount? =
    try {
        FeedItemsCount(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedItemsCount(val value: Long) {
    init {
        require(value >= 0) {
            "Items count should be greater or equal to 0"
        }
    }
}