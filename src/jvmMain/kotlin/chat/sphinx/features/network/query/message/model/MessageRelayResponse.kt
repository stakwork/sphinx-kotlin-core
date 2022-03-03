package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class MessageRelayResponse(
    override val success: Boolean,
    override val response: MessageDto?,
    override val error: String?
): RelayResponse<MessageDto>()
