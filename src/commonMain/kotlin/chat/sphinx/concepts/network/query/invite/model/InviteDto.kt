package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class InviteDto(
    val id: Long,
    val invite_string: String,
    val invoice: String? = null,
    val welcome_message: String,
    val contact_id: Long,
    val status: Int,
    val price: Long? = null,
    val created_at: String,
    val updated_at: String,
)
