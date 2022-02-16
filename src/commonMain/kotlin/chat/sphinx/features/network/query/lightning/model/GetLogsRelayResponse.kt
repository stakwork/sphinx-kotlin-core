package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetLogsRelayResponse(
    override val success: Boolean,
    override val response: String?,
    override val error: String?
): RelayResponse<String>()
