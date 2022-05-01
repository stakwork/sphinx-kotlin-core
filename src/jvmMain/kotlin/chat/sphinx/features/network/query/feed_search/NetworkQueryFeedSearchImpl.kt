package chat.sphinx.features.network.query.feed_search

import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.feed_search.model.FeedSearchResultDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.isPodcast
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

class NetworkQueryFeedSearchImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryFeedSearch() {

    companion object {
        private const val TRIBES_DEFAULT_SERVER_URL = "https://tribes.sphinx.chat"

        private const val ENDPOINT_PODCAST_SEARCH = "$TRIBES_DEFAULT_SERVER_URL/search_podcasts?q=%s"
        private const val ENDPOINT_YOUTUBE_SEARCH = "$TRIBES_DEFAULT_SERVER_URL/search_youtube?q=%s"
    }

    override fun searchFeeds(
        searchTerm: String,
        feedType: FeedType,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>?
    ): Flow<LoadResponse<List<FeedSearchResultDto>, ResponseError>> =
        networkRelayCall.getList(
            url = if (feedType.isPodcast())
                "$TRIBES_DEFAULT_SERVER_URL/search_podcasts?q=$searchTerm"
            else
                "$TRIBES_DEFAULT_SERVER_URL/search_youtube?q=$searchTerm",
            responseJsonSerializer = FeedSearchResultDto.serializer(),
        )

}