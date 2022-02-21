package chat.sphinx.concepts.link_preview.model

import chat.sphinx.wrapper.PhotoUrl
import io.ktor.http.*
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPreviewImageUrlOrNull(): PreviewImageUrl? =
    try {
        PreviewImageUrl(this)
    } catch (e: URLParserException) {
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
        Url(value)
    }
}
