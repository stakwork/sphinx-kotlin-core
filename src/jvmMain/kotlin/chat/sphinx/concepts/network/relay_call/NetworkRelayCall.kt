package chat.sphinx.concepts.network.relay_call

import chat.sphinx.concepts.network.call.NetworkCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

abstract class NetworkRelayCall: NetworkCall() {

    abstract fun getRawJson(
        url: String,
        headers: Map<String, String>? = null,
        useExtendedNetworkCallClient: Boolean = false
    ): Flow<LoadResponse<String, ResponseError>>

}
