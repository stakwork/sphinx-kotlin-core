package chat.sphinx.wrapper.lsat

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.lightning.LightningPaymentRequest

data class Lsat(
    val id: LsatIdentifier,
    val macaroon: Macaroon,
    val paymentRequest: LightningPaymentRequest?,
    val issuer: LsatIssuer?,
    val metaData: LsatMetaData?,
    val paths: LsatPaths?,
    val preimage: LsatPreImage?,
    val status: LsatStatus,
    val createdAt: DateTime
)