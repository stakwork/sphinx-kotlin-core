package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toFeedEnclosureLength(): FeedEnclosureLength? =
    try {
        FeedEnclosureLength(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedEnclosureLength(val value: Long) {
    init {
        require(value >= 0) {
            "FeedEnclosureLength must be greater than or equal to 0"
        }
    }
}