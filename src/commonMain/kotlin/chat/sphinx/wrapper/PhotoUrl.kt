package chat.sphinx.wrapper

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPhotoUrl(): PhotoUrl? =
    try {
        PhotoUrl(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
inline val PhotoUrl.thumbnailUrl: PhotoUrl
    get() = PhotoUrl(this.value + "?thumb=true")

@Suppress("NOTHING_TO_INLINE")
inline val PhotoUrl.notThumbnailUrl: PhotoUrl
    get() = PhotoUrl(this.value.replace("?thumb=true", ""))

@Suppress("NOTHING_TO_INLINE")
inline val PhotoUrl.isThumbnailUrl: Boolean
    get() = this.value.contains("?thumb=true")

@JvmInline
value class PhotoUrl(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "PhotoUrl cannot be empty"
        }
    }
}
