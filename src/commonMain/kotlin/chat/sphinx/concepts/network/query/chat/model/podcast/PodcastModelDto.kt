package chat.sphinx.concepts.network.query.chat.model.podcast

import kotlinx.serialization.Serializable

@Serializable
data class PodcastModelDto(
    val type: String,
    val suggested: Double,
)