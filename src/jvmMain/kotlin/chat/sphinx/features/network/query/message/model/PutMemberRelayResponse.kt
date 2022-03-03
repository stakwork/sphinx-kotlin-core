package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.PutMemberResponseDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PutMemberRelayResponse(
    override val success: Boolean,
    override val response: PutMemberResponseDto?,
    override val error: String?
): RelayResponse<PutMemberResponseDto>()
