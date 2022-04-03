package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.invoice.InvoiceDto
import chat.sphinx.concepts.network.query.lightning.model.invoice.InvoicesDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

/**
 * The endpoint doesn't return a [RelayResponse], yet one is required for the call. This,
 * is simply a work around for Moshi and the call adapter.
 * */
@Serializable
data class GetInvoicesRelayResponse(
    val invoices: List<InvoiceDto>,
    val last_index_offset: Long,
    val first_index_offset: Long,

    override val success: Boolean = true,
    override val response: InvoicesDto = InvoicesDto(invoices, last_index_offset, first_index_offset),
    override val error: String? = null
): RelayResponse<InvoicesDto>()
