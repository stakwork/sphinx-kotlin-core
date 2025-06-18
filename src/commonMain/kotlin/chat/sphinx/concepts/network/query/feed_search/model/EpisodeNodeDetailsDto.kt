package chat.sphinx.concepts.network.query.feed_search.model

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeNodeDetailsDto(
    val name: String? = null,
    val node_type: String? = null,
    val properties: EpisodeNodeProperties? = null,
    val ref_id: String? = null
)

@Serializable
data class EpisodeNodeProperties(
    val date: Long? = null,
    val episode_title: String? = null,
    val image_url: String? = null,
    val media_url: String? = null,
    val project_id: String? = null,
    val source_link: String? = null,
    val status: String? = null
)
