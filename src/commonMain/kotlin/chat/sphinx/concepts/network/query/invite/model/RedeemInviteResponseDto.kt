package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class RedeemInviteResponseDto(
    val ip: String,
    val invite: RedeemInviteDto,
    val pubkey: String? = null
)
