package chat.sphinx.concepts.network.query.chat.model.podcast

import kotlinx.serialization.Serializable

@Serializable
data class PodcastEpisodeDto(
    val id: Long,
    val title: String,
    val description: String,
    val image: String,
    val link: String,
    val enclosureUrl: String,
)