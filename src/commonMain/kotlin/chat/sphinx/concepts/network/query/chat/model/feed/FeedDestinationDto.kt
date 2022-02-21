package chat.sphinx.concepts.network.query.chat.model.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedDestinationDto(
    val split: Long,
    val address: String,
    val type: String,
    val customKey: String?,
    val customValue: String?,
)