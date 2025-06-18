package chat.sphinx.concepts.network.query.feed_search

import chat.sphinx.concepts.network.query.feed_search.model.CreateProjectResponseDto
import chat.sphinx.concepts.network.query.feed_search.model.EpisodeNodeDetailsDto
import chat.sphinx.concepts.network.query.feed_search.model.EpisodeNodeResponseDto
import chat.sphinx.concepts.network.query.feed_search.model.FeedSearchResultDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.feed.FeedReferenceId
import chat.sphinx.wrapper.feed.FeedTitle
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.podcast.ChapterResponseDto
import chat.sphinx.wrapper.podcast.PodcastEpisode
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryFeedSearch {

    ///////////
    /// GET ///
    ///////////
    abstract fun searchFeeds(
        searchTerm: String,
        feedType: FeedType,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<List<FeedSearchResultDto>, ResponseError>>

    abstract fun checkIfEpisodeNodeExists(episode: PodcastEpisode, feedTitle: FeedTitle): Flow<LoadResponse<EpisodeNodeResponseDto, ResponseError>>
    abstract fun createStakworkProject(
        podcastEpisode: PodcastEpisode,
        feedTitle: FeedTitle,
        workflowId: Int,
        token: String,
        referenceId: FeedReferenceId
    ): Flow<LoadResponse<CreateProjectResponseDto, ResponseError>>

    abstract fun getEpisodeNodeDetails(referenceId: FeedReferenceId): Flow<LoadResponse<EpisodeNodeDetailsDto, ResponseError>>

    abstract fun getChaptersData(referenceId: FeedReferenceId): Flow<LoadResponse<ChapterResponseDto, ResponseError>>
}