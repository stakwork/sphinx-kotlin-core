package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.KeySendPaymentDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class KeySendPaymentRelayResponse(
    override val success: Boolean,
    override val response: KeySendPaymentDto?,
    override val error: String?
): RelayResponse<KeySendPaymentDto>()