package chat.sphinx.concepts.network.query.redeem_badge_token

import chat.sphinx.concepts.network.query.redeem_badge_token.model.RedeemBadgeTokenDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryRedeemBadgeToken{
    abstract fun redeemBadgeToken(
        data: RedeemBadgeTokenDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>

} 

