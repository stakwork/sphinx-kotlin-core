package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PostGroupRelayResponse(
    override val success: Boolean,
    override val response: ChatDto? = null,
    override val error: String? = null
): RelayResponse<ChatDto>()