package chat.sphinx.concepts.network.query.chat.model.podcast

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PodcastValueDto(
    val model: PodcastModelDto,
    val destinations: List<PodcastDestinationDto>,
)