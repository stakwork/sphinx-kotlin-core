package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.invoice.PaymentMessageDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PayLightningPaymentRequestRelayResponse(
    override val success: Boolean,
    override val response: PaymentMessageDto? = null,
    override val error: String? = null
): RelayResponse<PaymentMessageDto>()
