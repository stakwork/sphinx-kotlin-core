package chat.sphinx.concepts.network.query.chat.model.podcast

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PodcastEpisodeDto(
    val id: Long,
    val title: String,
    val description: String,
    val image: String,
    val link: String,
    val enclosureUrl: String,
)