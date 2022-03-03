package chat.sphinx.features.network.query.message.model

import kotlinx.serialization.Serializable

@Serializable
data class PostPayAttachmentDto(
    val chat_id: Long,
    val contact_id: Long?,
    val amount: Long,
    val media_token: String,
)