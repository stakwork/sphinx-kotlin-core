package chat.sphinx.features.network.query.redeem_badge_token

import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.network.query.redeem_badge_token.model.RedeemBadgeTokenDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.redeem_badge_token.model.RedeemBadgeTokenResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

class NetworkQueryRedeemBadgeTokenImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryRedeemBadgeToken() {

    companion object {
        private const val ENDPOINT_CLAIM_ON_LIQUID = "/claim_on_liquid"
    }

}
