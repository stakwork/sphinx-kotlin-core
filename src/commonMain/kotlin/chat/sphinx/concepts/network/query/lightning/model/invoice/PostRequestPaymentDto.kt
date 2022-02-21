package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class PostRequestPaymentDto(
    val amount: Long,
    val memo: String? = null
)