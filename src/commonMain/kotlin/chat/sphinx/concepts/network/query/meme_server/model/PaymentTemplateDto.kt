package chat.sphinx.concepts.network.query.meme_server.model

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
data class PaymentTemplateDto(
    val muid: String,
    val width: Int,
    val height: Int,
)