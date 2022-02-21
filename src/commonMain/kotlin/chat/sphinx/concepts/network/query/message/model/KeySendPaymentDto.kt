package chat.sphinx.concepts.network.query.message.model

import kotlinx.serialization.Serializable

@Serializable
data class KeySendPaymentDto(
    val amount: Long,
    val destination_key: String,
) {}