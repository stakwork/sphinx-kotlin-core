package chat.sphinx.concepts.link_preview.model

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toHtmlPreviewTitleOrNull(): HtmlPreviewTitle? =
    try {
        HtmlPreviewTitle(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class HtmlPreviewTitle(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "HtmlPreviewTitle cannot be empty"
        }
    }
}
