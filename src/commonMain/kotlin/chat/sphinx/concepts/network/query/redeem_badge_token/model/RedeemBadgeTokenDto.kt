package chat.sphinx.concepts.network.query.redeem_badge_token.model

import kotlinx.serialization.Serializable

@Serializable
data class RedeemBadgeTokenDto(
    val host: String,
    val amount: Int,
    val to: String,
    val asset: Int,
    val memo: String
) 
