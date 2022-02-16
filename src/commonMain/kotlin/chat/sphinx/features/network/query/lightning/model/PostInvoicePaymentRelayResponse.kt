package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.invoice.LightningPaymentInvoiceDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PostInvoicePaymentRelayResponse(
    override val success: Boolean,
    override val response: LightningPaymentInvoiceDto?,
    override val error: String?
): RelayResponse<LightningPaymentInvoiceDto>()
