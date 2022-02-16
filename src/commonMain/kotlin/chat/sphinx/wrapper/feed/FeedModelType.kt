package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedModelType(): FeedModelType? =
    try {
        FeedModelType(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class FeedModelType(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "FeedModelType cannot be empty"
        }
    }
}