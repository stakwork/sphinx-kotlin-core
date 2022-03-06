package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMessageDto(
    val success: Boolean,
    val response: PayRequestDto
    )
