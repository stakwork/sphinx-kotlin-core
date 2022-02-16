package chat.sphinx.concepts.network.query.chat.model.podcast

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PodcastDestinationDto(
    val split: Long,
    val address: String,
    val type: String,
)