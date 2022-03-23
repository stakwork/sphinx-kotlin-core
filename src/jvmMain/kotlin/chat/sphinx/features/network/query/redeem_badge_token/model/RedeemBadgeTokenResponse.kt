package chat.sphinx.features.network.query.redeem_badge_token.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable


@Serializable
data class RedeemBadgeTokenResponse(
    override val success: Boolean = true,
    override val response: @Polymorphic Any?,
    override val error: String?
): RelayResponse<Any>()
