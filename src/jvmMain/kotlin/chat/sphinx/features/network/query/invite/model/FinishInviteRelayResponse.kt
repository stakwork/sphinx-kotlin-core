package chat.sphinx.features.network.query.invite.model

import chat.sphinx.concepts.network.query.invite.model.FinishInviteResponseDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
class FinishInviteRelayResponse(
    override val success: Boolean,
    @SerialName("object")
    @JsonNames("object")
    override val response: FinishInviteResponseDto? = null,
    override val error: String? = null
): RelayResponse<FinishInviteResponseDto>()
