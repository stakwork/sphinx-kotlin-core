package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.GetMessagesResponse
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class GetMessagesRelayResponse(
    override val success: Boolean,
    override val response: GetMessagesResponse?,
    override val error: String?
) : RelayResponse<GetMessagesResponse>()
