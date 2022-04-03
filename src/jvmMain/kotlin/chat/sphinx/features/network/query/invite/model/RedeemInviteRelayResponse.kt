package chat.sphinx.features.network.query.invite.model

import chat.sphinx.concepts.network.query.invite.model.RedeemInviteResponseDto
import chat.sphinx.concepts.network.relay_call.RelayResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
class RedeemInviteRelayResponse(
    // I marked this as default true because Hub doesn't return a success field but I need one
    // here because it inherits it from RelayResponse
    override val success: Boolean = true,

    @JsonNames("object")
    override val response: RedeemInviteResponseDto? = null,

    override val error: String? = null
): RelayResponse<RedeemInviteResponseDto>()
