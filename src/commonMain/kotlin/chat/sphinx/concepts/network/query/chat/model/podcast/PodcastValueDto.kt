package chat.sphinx.concepts.network.query.chat.model.podcast

import kotlinx.serialization.Serializable

@Serializable
data class PodcastValueDto(
    val model: PodcastModelDto,
    val destinations: List<PodcastDestinationDto>,
)