package chat.sphinx.concepts.network.query.lightning.model.lightning

import kotlinx.serialization.Serializable

@Serializable
data class ActiveLsatDto(
    val macaroon: String,
    val identifier: String,
    val preimage: String,
    val paymentRequest: String,
    val issuer: String,
    val status: Long,
    val paths: String?
)
