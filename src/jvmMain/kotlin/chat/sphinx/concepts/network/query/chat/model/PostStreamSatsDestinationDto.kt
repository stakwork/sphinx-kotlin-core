package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class PostStreamSatsDestinationDto(
    val address: String,
    val type: String,
    val split: Double,
)