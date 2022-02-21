package chat.sphinx.features.link_preview

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.link_preview.LinkPreviewHandler
import chat.sphinx.concepts.link_preview.model.HtmlPreviewData
import chat.sphinx.concepts.link_preview.model.TribePreviewData
import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.wrapper.tribe.TribeJoinLink

class LinkPreviewHandlerImpl(
    private val dispatchers: CoroutineDispatchers,
    private val networkClient: NetworkClient,
    private val networkQueryChat: NetworkQueryChat,
) : LinkPreviewHandler() {

    override suspend fun retrieveHtmlPreview(url: String): HtmlPreviewData? {
       return LinkPreviewCache.getInstance()
           .getHtmlPreviewDataRetriever(url)
           ?.getHtmlPreview(dispatchers, networkClient.getClient())
    }

    override suspend fun retrieveTribeLinkPreview(tribeJoinLink: TribeJoinLink): TribePreviewData? {
        return LinkPreviewCache.getInstance()
            .getTribePreviewDataRetriever(tribeJoinLink)
            ?.getTribePreview(networkQueryChat)
    }
}
