package chat.sphinx.concepts.network.query.relay_keys

import chat.sphinx.concepts.network.query.relay_keys.model.PostHMacKeyDto
import chat.sphinx.concepts.network.query.relay_keys.model.RelayHMacKeyDto
import chat.sphinx.concepts.network.query.relay_keys.model.RelayTransportKeyDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryRelayKeys {

    ///////////
    /// GET ///
    ///////////
    abstract fun getRelayTransportKey(
        relayUrl: RelayUrl
    ): Flow<LoadResponse<RelayTransportKeyDto, ResponseError>>

    abstract fun getRelayHMacKey(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<RelayHMacKeyDto, ResponseError>>

    ////////////
    /// POST ///
    ////////////
    abstract fun addRelayHMacKey(
        addHMacKeyDto: PostHMacKeyDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any?, ResponseError>>
}