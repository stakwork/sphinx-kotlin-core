package chat.sphinx.wrapper.message.media.token

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMediaMUIDOrNull(): MediaMUID? =
    try {
        MediaMUID(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class MediaMUID(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "MediaMUID cannot be empty"
        }
    }
}
