package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class DeleteChatRelayResponse(
    override val success: Boolean,
    override val response: Map<String, Long>?,
    override val error: String?
): RelayResponse<Map<String, Long>>()