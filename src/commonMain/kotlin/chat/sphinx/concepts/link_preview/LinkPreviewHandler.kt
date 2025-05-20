package chat.sphinx.concepts.link_preview

import chat.sphinx.concepts.link_preview.model.HtmlPreviewData
import chat.sphinx.concepts.link_preview.model.TribePreviewData
import chat.sphinx.wrapper.tribe.TribeJoinLink

abstract class LinkPreviewHandler {
    abstract suspend fun retrieveHtmlPreview(url: String): HtmlPreviewData?
    abstract suspend fun retrieveTribeLinkPreview(tribeJoinLink: TribeJoinLink, isProductionEnvironment: Boolean): TribePreviewData?
}
