package chat.sphinx.concepts.network.query.contact

import chat.sphinx.concepts.network.query.contact.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.contact.Blocked
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryContact {

    abstract fun hasAdmin(
        url: RelayUrl
    ): Flow<LoadResponse<Any, ResponseError>>

    abstract fun getAccountConfig(isProductionEnvironment: Boolean): Flow<LoadResponse<AccountConfigV2Response, ResponseError>>

    abstract fun getNodes(
        routerUrl: String
    ): Flow<LoadResponse<String, ResponseError>>

    abstract fun getRoutingNodes(
        routerUrl: String,
        lightningNodePubKey: LightningNodePubKey,
        milliSats: Long
    ): Flow<LoadResponse<String, ResponseError>>

}
