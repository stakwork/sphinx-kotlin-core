package chat.sphinx.wrapper.podcast

import kotlinx.serialization.Serializable

@Serializable
data class ChapterResponseDto(
    val edges: List<EdgeDto>,
    val nodes: List<NodeDto>,
    val status: String
)

@Serializable
data class EdgeDto(
    val edge_type: String,
    val ref_id: String,
    val source: String,
    val target: String,
    val weight: Int,
    val properties: Map<String, String>?
)

@Serializable
data class NodeDto(
    val ref_id: String,
    val node_type: String,
    val date_added_to_graph: Double,
    val properties: ChapterProperties?
)

@Serializable
data class ChapterProperties(
    val is_ad: String?,
    val name: String?,
    val source_link: String?,
    val timestamp: String?,
    val episode_title: String?,
    val image_url: String?,
    val media_url: String?,
    val status: String?,
    val date: Long?
) {
    val isAdBoolean: Boolean
        get() = is_ad?.equals("True", ignoreCase = true) == true

}