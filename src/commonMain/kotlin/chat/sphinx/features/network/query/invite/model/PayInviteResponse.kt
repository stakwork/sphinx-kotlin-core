package chat.sphinx.features.network.query.invite.model

import chat.sphinx.concepts.network.query.invite.model.PayInviteDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PayInviteResponse(
    override val success: Boolean,
    override val response: PayInviteDto,
    override val error: String? = null
): RelayResponse<PayInviteDto>()
