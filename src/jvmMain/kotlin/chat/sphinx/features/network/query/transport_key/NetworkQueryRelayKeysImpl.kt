package chat.sphinx.features.network.query.transport_key


import chat.sphinx.concepts.network.query.relay_keys.NetworkQueryRelayKeys
import chat.sphinx.concepts.network.query.relay_keys.model.PostHMacKeyDto
import chat.sphinx.concepts.network.query.relay_keys.model.RelayHMacKeyDto
import chat.sphinx.concepts.network.query.relay_keys.model.RelayTransportKeyDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.chat.NetworkQueryChatImpl
import chat.sphinx.features.network.query.chat.model.PostGroupRelayResponse
import chat.sphinx.features.network.query.transport_key.model.AddRelayHMacKeyResponse
import chat.sphinx.features.network.query.transport_key.model.GetHMacKeyRelayResponse
import chat.sphinx.features.network.query.transport_key.model.GetTransportKeyRelayResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

class NetworkQueryRelayKeysImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryRelayKeys() {

    companion object {
        private const val ENDPOINT_TRANSPORT_KEY = "/request_transport_key"
        private const val ENDPOINT_H_MAC_KEY = "/hmac_key"
    }

    override fun getRelayTransportKey(
        relayUrl: RelayUrl
    ): Flow<LoadResponse<RelayTransportKeyDto, ResponseError>> {
        return networkRelayCall.relayUnauthenticatedGet(
            responseJsonSerializer = GetTransportKeyRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_TRANSPORT_KEY,
            relayUrl = relayUrl
        )
    }

    override fun getRelayHMacKey(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<RelayHMacKeyDto, ResponseError>> {
        return networkRelayCall.relayGet(
            responseJsonSerializer = GetHMacKeyRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_H_MAC_KEY,
            relayData = relayData
        )
    }

    override fun addRelayHMacKey(
        addHMacKeyDto: PostHMacKeyDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<Any?, ResponseError>> {
        return networkRelayCall.relayPost(
            responseJsonSerializer = AddRelayHMacKeyResponse.serializer(),
            relayEndpoint = ENDPOINT_H_MAC_KEY,
            requestBodyPair = Pair(
                addHMacKeyDto,
                PostHMacKeyDto.serializer()
            ),
            relayData = relayData
        )
    }
}