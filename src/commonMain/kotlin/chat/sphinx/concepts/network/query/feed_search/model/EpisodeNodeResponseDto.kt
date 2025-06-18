package chat.sphinx.concepts.network.query.feed_search.model

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeNodeResponseDto(
    val data: EpisodeNodeDataDto? = null,
    val success: Boolean? = null,
    val errorCode: String? = null,
    val message: String? = null,
    val node_key: String? = null,
    val status: String? = null,
    val status_messages: List<String>? = null
)

@Serializable
data class EpisodeNodeDataDto(
    val project_id: Long? = null,
    val node_key: String? = null,
    val ref_id: String
)
