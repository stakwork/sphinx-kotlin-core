package chat.sphinx.concepts.network.query.transport_key

import chat.sphinx.concepts.network.query.transport_key.model.RelayTransportKeyDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryTransportKey {

    ///////////
    /// GET ///
    ///////////
    abstract fun getRelayTransportKey(
        relayUrl: RelayUrl
    ): Flow<LoadResponse<RelayTransportKeyDto, ResponseError>>
}