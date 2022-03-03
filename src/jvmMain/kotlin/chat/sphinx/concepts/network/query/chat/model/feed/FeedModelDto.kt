package chat.sphinx.concepts.network.query.chat.model.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedModelDto(
    val type: String,
    val suggested: Double,
)