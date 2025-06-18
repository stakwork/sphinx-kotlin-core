package chat.sphinx.concepts.network.query.feed_search.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedRecommendationDto(
    val pub_key: String,
    val type: String,
    val ref_id: String,
    val topics: List<String>,
    val weight: Float,
    val description: String,
    val date: Long?,
    val show_title: String,
    val boost: Long,
    val keyword: String? = null,
    val s_image_url: String? = null,
    val m_image_url: String? = null,
    val l_image_url: String? = null,
    val node_type: String,
    val hosts: List<Hosts>,
    val guests: List<String>,
    val text: String,
    val timestamp: String,
    val episode_title: String,
    val guest_profiles: List<String?>,
    val link: String,
)

@Serializable
data class Hosts(
    val name: String,
    val twitter_handle: String,
    val profile_picture: String
)