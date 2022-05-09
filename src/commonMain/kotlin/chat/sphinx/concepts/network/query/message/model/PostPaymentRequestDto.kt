package chat.sphinx.concepts.network.query.message.model

import kotlinx.serialization.Serializable

@Serializable
data class PostPaymentRequestDto(
    val chat_id: Long? = null,
    val contact_id: Long? = null,
    val amount: Long,
    val memo: String? = null,
    val remote_memo: String? = null,
)