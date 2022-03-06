package chat.sphinx.concepts.network.query.chat.model.podcast

import kotlinx.serialization.Serializable

@Serializable
data class PodcastDestinationDto(
    val split: Long,
    val address: String,
    val type: String,
)