package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class RedeemInviteDto(
    val id: Long,
    val node_id: Long,
    val invite_status: Int,
    val pubkey: String? = null,
    val expires_on: String? = null,
    val fee_paid: Long? = null,
    val message: String? = null,
    var nickname: String? = null,
    var pin: String? = null,
    val created_at: String,
    val updated_at: String,
    var contact_nickname: String? = null,
    var invoice: String? = null,
    var action: String? = null,
    var route_hint: String? = null,
)
