package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class PayRequestDto(val payment_request: String)
