package chat.sphinx.concepts.link_preview.model

import io.ktor.http.*
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toHtmlPreviewFavIconUrlOrNull(): HtmlPreviewFavIconUrl? =
    try {
        HtmlPreviewFavIconUrl(this)
    } catch (e: Throwable) {
        null
    }

@JvmInline
value class HtmlPreviewFavIconUrl(val value: String) {
    init {
        Url(value)
    }
}
