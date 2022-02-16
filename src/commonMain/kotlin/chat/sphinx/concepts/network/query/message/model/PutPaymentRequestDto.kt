package chat.sphinx.concepts.network.query.message.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PutPaymentRequestDto(
    val payment_request: String
)