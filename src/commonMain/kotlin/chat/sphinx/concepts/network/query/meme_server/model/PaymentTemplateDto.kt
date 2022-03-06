package chat.sphinx.concepts.network.query.meme_server.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentTemplateDto(
    val muid: String,
    val width: Int,
    val height: Int,
)