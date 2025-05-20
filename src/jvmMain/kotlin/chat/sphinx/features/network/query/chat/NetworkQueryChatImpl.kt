package chat.sphinx.features.network.query.chat

import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.chat.model.*
import chat.sphinx.concepts.network.query.chat.model.feed.FeedDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.feed.FeedUrl
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import kotlinx.coroutines.flow.Flow

class NetworkQueryChatImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryChat() {

    companion object {
        private const val GET_TRIBE_INFO_URL_TEST = "http://%s/tribes/%s"
        private const val GET_TRIBE_INFO_URL_PRODUCTION = "https://%s/tribes/%s"
        private const val GET_FEED_CONTENT_URL = "https://%s/feed?url=%s&fulltext=true"
        private const val TEST_V2_TRIBES_SERVER = "75.101.247.127:8801"
        private const val FEED_SPHINX_V1_URL = "https://people.sphinx.chat/feed?url=%s"

    }

    override fun getTribeInfo(
        host: ChatHost,
        tribePubKey: LightningNodePubKey,
        isProductionEnvironment: Boolean
    ): Flow<LoadResponse<NewTribeDto, ResponseError>> =
        networkRelayCall.get(
            url = String.format(
                if (isProductionEnvironment) GET_TRIBE_INFO_URL_PRODUCTION else GET_TRIBE_INFO_URL_TEST,
                if (isProductionEnvironment) host.value else TEST_V2_TRIBES_SERVER,
                tribePubKey.value
            ),
            responseJsonSerializer = NewTribeDto.serializer()
        )

    override fun getFeedContent(
        host: ChatHost,
        feedUrl: FeedUrl,
        chatUUID: ChatUUID?,
    ): Flow<LoadResponse<FeedDto, ResponseError>> =
        networkRelayCall.get(
            url = if (chatUUID != null) {
                "${String.format(FEED_SPHINX_V1_URL, feedUrl.value)}&uuid=${chatUUID.value}"
            } else {
                String.format(FEED_SPHINX_V1_URL, feedUrl.value)
            },
            responseJsonSerializer = FeedDto.serializer()
        )
}
