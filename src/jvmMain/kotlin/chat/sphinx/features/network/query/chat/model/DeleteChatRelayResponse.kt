package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class DeleteChatRelayResponse(
    override val success: Boolean,
    override val response: Map<String, Long>? = null,
    override val error: String? = null
): RelayResponse<Map<String, Long>>()