package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class GetChatsRelayResponse(
    override val success: Boolean,
    override val response: List<ChatDto>? = null,
    override val error: String? = null
): RelayResponse<List<ChatDto>>()
