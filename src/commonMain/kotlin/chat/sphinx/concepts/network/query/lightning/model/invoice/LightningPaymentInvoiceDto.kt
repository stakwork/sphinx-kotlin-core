package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class LightningPaymentInvoiceDto(val invoice: String)
