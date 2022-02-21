package chat.sphinx.concepts.network.query.message.model

import kotlinx.serialization.Serializable

@Serializable
data class PutPaymentRequestDto(
    val payment_request: String
)