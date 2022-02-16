package chat.sphinx.concepts.network.query.message.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeySendPaymentDto(
    val amount: Long,
    val destination_key: String,
) {}