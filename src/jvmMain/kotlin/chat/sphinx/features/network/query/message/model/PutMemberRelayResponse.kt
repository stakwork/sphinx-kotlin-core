package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.PutMemberResponseDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PutMemberRelayResponse(
    override val success: Boolean,
    override val response: PutMemberResponseDto? = null,
    override val error: String? = null
): RelayResponse<PutMemberResponseDto>()
