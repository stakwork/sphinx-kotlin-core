package chat.sphinx.features.network.query.transport_key.model

import chat.sphinx.concepts.network.query.relay_keys.model.RelayTransportKeyDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetTransportKeyRelayResponse(
    override val success: Boolean,
    override val response: RelayTransportKeyDto? = null,
    override val error: String? = null
): RelayResponse<RelayTransportKeyDto>()