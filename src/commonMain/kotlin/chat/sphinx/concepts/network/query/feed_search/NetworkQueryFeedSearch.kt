package chat.sphinx.concepts.network.query.feed_search

import chat.sphinx.concepts.network.query.feed_search.model.FeedSearchResultDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryFeedSearch {

    ///////////
    /// GET ///
    ///////////
    abstract fun searchFeeds(
        searchTerm: String,
        feedType: FeedType,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<List<FeedSearchResultDto>, ResponseError>>
}