package chat.sphinx.wrapper.message.media

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMediaKey(): MediaKey? =
    MediaKey(this)

@JvmInline
value class MediaKey(val value: String)
