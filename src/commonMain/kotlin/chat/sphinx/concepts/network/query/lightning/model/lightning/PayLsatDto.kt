package chat.sphinx.concepts.network.query.lightning.model.lightning

import kotlinx.serialization.Serializable

@Serializable
data class PayLsatDto(
    val macaroon: String,
    val paymentRequest: String,
    val issuer: String,
)
