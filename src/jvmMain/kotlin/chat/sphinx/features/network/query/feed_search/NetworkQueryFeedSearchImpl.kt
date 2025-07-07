package chat.sphinx.features.network.query.feed_search

import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.feed_search.model.CreateProjectResponseDto
import chat.sphinx.concepts.network.query.feed_search.model.EpisodeNodeDetailsDto
import chat.sphinx.concepts.network.query.feed_search.model.EpisodeNodeResponseDto
import chat.sphinx.concepts.network.query.feed_search.model.FeedSearchResultDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.feed.FeedReferenceId
import chat.sphinx.wrapper.feed.FeedTitle
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.isPodcast
import chat.sphinx.wrapper.podcast.ChapterResponseDto
import chat.sphinx.wrapper.podcast.PodcastEpisode
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import chat.sphinx.wrapper.time
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

class NetworkQueryFeedSearchImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryFeedSearch() {

    companion object {
        private const val TRIBES_DEFAULT_SERVER_URL = "https://people.sphinx.chat"

        private const val GRAPH_MINDSET_BASE_URL = "https://graphmindset.sphinx.chat"
        private const val GRAPH_MINDSET_ADD_NODE_URL = "https://graphmindset.sphinx.chat/api/add_node?sig=&msg="
        private const val ENDPOINT_GET_CHAPTERS = "$GRAPH_MINDSET_BASE_URL/api/graph/subgraph?start_node=%s&include_properties=true&depth=1&node_type=%%5B%%27Chapter%%27%%5D"
        private const val ENDPOINT_STAKWORK_PROJECT = "https://api.stakwork.com/api/v1/projects"

        private const val ENDPOINT_PODCAST_SEARCH = "$TRIBES_DEFAULT_SERVER_URL/search_podcasts?q=%s"
        private const val ENDPOINT_YOUTUBE_SEARCH = "$TRIBES_DEFAULT_SERVER_URL/search_youtube?q=%s"
    }

    override fun searchFeeds(
        searchTerm: String,
        feedType: FeedType,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<List<FeedSearchResultDto>, ResponseError>> =
        networkRelayCall.getList(
            url = String.format(
                if (feedType.isPodcast())
                    ENDPOINT_PODCAST_SEARCH
                else
                    ENDPOINT_YOUTUBE_SEARCH, searchTerm
            ),
            responseJsonSerializer = FeedSearchResultDto.serializer(),
        )

    override fun checkIfEpisodeNodeExists(
        episode: PodcastEpisode,
        feedTitle: FeedTitle
    ): Flow<LoadResponse<EpisodeNodeResponseDto, ResponseError>> {

        val nodeData = mapOf(
            "source_link" to episode.enclosureUrl.value,
            "date" to episode.date?.time?.div(1000),
            "episode_title" to episode.title.value,
            "image_url" to episode.image?.value,
            "show_title" to feedTitle.value
        ).filterValues { it != null }

        val requestBodyRaw = mapOf(
            "node_type" to "Episode",
            "node_data" to nodeData
        )

        val requestBodyJsonElement = buildJsonObject {
            put("node_type", JsonPrimitive("Episode"))
            put("node_data", buildJsonObject {
                put("source_link", JsonPrimitive(episode.enclosureUrl.value))
                episode.date?.value?.local?.unixMillisLong?.let {
                    put("date", JsonPrimitive(it / 1000))
                }
                put("episode_title", JsonPrimitive(episode.title.value))
                episode.image?.value?.let {
                    put("image_url", JsonPrimitive(it))
                }
                put("show_title", JsonPrimitive(feedTitle.value))
            })
        }

        return networkRelayCall.post(
            url = GRAPH_MINDSET_ADD_NODE_URL,
            responseJsonSerializer = EpisodeNodeResponseDto.serializer(),
            requestBodyPair = Pair(
                requestBodyJsonElement,
                MapSerializer(String.serializer(), JsonElement.serializer())
            ),            mediaType = "application/json",
            accept400AsSuccess = true
        )
    }

    override fun createStakworkProject(
        podcastEpisode: PodcastEpisode,
        feedTitle: FeedTitle,
        workflowId: Int,
        token: String,
        referenceId: FeedReferenceId
    ): Flow<LoadResponse<CreateProjectResponseDto, ResponseError>> {

        val mediaUrl = podcastEpisode.enclosureUrl.value
        val episodePublishDate = podcastEpisode.date?.time?.div(1000) ?: 0
        val episodeThumbnailUrl = podcastEpisode.image?.value ?: ""

        val requestBodyJsonElement = buildJsonObject {
            put("name", JsonPrimitive(mediaUrl))
            put("workflow_id", JsonPrimitive(workflowId))
            put("workflow_params", buildJsonObject {
                put("set_var", buildJsonObject {
                    put("attributes", buildJsonObject {
                        put("vars", buildJsonObject {
                            put("media_url", JsonPrimitive(mediaUrl))
                            put("ref_id", JsonPrimitive(referenceId.value))
                            put("episode_publish_date", JsonPrimitive(episodePublishDate))
                            put("episode_title", JsonPrimitive(podcastEpisode.title.value))
                            put("episode_thumbnail_url", JsonPrimitive(episodeThumbnailUrl))
                            put("show_title", JsonPrimitive(feedTitle.value))
                        })
                    })
                })
            })
        }

        return networkRelayCall.post(
            url = ENDPOINT_STAKWORK_PROJECT,
            responseJsonSerializer = CreateProjectResponseDto.serializer(),
            requestBodyPair = Pair(
                requestBodyJsonElement,
                JsonObject.serializer()
            ),
            mediaType = "application/json",
            headers = mapOf("Authorization" to "Bearer $token"),
        )
    }

    override fun getEpisodeNodeDetails(referenceId: FeedReferenceId): Flow<LoadResponse<EpisodeNodeDetailsDto, ResponseError>> {
        val url = "$GRAPH_MINDSET_BASE_URL/api/node/${referenceId.value}"

        return networkRelayCall.get(
            url = url,
            responseJsonSerializer = EpisodeNodeDetailsDto.serializer()
        )
    }

    override fun getChaptersData(referenceId: FeedReferenceId): Flow<LoadResponse<ChapterResponseDto, ResponseError>> {
        return networkRelayCall.get(
            url = String.format(ENDPOINT_GET_CHAPTERS, referenceId.value),
            responseJsonSerializer = ChapterResponseDto.serializer()
        )
    }

}