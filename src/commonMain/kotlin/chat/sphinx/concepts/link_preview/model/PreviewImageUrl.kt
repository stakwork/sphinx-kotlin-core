package chat.sphinx.concepts.link_preview.model

import chat.sphinx.wrapper.PhotoUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPreviewImageUrlOrNull(): PreviewImageUrl? =
    try {
        PreviewImageUrl(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
inline fun PreviewImageUrl.toPhotoUrl(): PhotoUrl =
    PhotoUrl(value)

@Suppress("NOTHING_TO_INLINE")
inline fun PhotoUrl.toPreviewImageUrlOrNull(): PreviewImageUrl? =
    value.toPreviewImageUrlOrNull()

@JvmInline
value class PreviewImageUrl(val value: String) {
    init {
        require(value.toHttpUrlOrNull() != null) {
            "PreviewImageUrl was not a valid url"
        }
    }
}
