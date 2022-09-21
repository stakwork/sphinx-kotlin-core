package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class FinishInviteResponseDto(
    val invite: RedeemInviteDto,
)
