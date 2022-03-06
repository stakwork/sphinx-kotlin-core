package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class JoinTribeRelayResponse(
    override val success: Boolean,
    override val response: chat.sphinx.concepts.network.query.chat.model.ChatDto,
    override val error: String?
): RelayResponse<ChatDto>()