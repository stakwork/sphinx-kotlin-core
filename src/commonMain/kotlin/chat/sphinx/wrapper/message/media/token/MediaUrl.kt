package chat.sphinx.wrapper.message.media.token

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMediaUrlOrNull(): MediaUrl? =
    try {
        MediaUrl(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class MediaUrl(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "MediaUrl cannot be empty"
        }
    }
}
