package chat.sphinx.concepts.network.query.feed_search.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedRecommendationsResponse(
    val recommendations: List<FeedRecommendationDto>
)