package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetLogsRelayResponse(
    override val success: Boolean,
    override val response: String? = null,
    override val error: String? = null
): RelayResponse<String>()
