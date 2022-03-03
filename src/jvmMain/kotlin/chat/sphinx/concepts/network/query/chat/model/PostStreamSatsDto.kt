package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class PostStreamSatsDto(
    val amount: Long,
    val chat_id: Long,
    val text: String,
    val update_meta: Boolean = true,
    val destinations: List<PostStreamSatsDestinationDto>
)