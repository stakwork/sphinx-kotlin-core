package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedDescription(): FeedDescription? =
    try {
        FeedDescription(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedDescription(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "FeedDescription cannot be empty"
        }
    }
}