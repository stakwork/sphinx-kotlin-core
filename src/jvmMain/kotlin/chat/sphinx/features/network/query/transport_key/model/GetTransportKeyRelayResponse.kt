package chat.sphinx.features.network.query.transport_key.model

import chat.sphinx.concepts.network.query.transport_key.model.RelayTransportKeyDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetTransportKeyRelayResponse(
    override val success: Boolean,
    override val response: RelayTransportKeyDto?,
    override val error: String?
): RelayResponse<RelayTransportKeyDto>()