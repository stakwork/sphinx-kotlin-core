package chat.sphinx.wrapper.podcast

import chat.sphinx.utils.SphinxJson
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.FeedBoost
import chat.sphinx.wrapper.message.PodBoostMoshi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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
    val properties: Map<String, String>? = null
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
    val is_ad: String? = null,
    val name: String? = null,
    val source_link: String? = null,
    val timestamp: String? = null,
    val episode_title: String? = null,
    val image_url: String? = null,
    val media_url: String? = null,
    val status: String? = null,
    val date: Long? = null
) {
    val isAdBoolean: Boolean
        get() = is_ad?.equals("True", ignoreCase = true) == true

}
fun String.toChapterResponseDto(): ChapterResponseDto =
    SphinxJson.decodeFromString<ChapterResponseDto>(this).let {
        ChapterResponseDto(
            edges = it.edges,
            nodes = it.nodes,
            status = it.status
        )
    }
fun ChapterResponseDto.toJson(): String =
    SphinxJson.encodeToString(this)