package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class PayInviteDto(
    val invite: InviteDto
)