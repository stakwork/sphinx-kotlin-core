package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.query.message.model.TransactionDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetPaymentsRelayResponse(
    override val success: Boolean,
    override val response: List<TransactionDto>?,
    override val error: String?
): RelayResponse<List<TransactionDto>>()
