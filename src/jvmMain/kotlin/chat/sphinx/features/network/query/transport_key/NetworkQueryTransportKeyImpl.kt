package chat.sphinx.features.network.query.transport_key


import chat.sphinx.concepts.network.query.transport_key.NetworkQueryTransportKey
import chat.sphinx.concepts.network.query.transport_key.model.RelayTransportKeyDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.transport_key.model.GetTransportKeyRelayResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow

class NetworkQueryTransportKeyImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryTransportKey() {

    companion object {
        private const val ENDPOINT_TRANSPORT_KEY = "/request_transport_key"
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
}