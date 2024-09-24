package chat.sphinx.features.network.query.contact

import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.contact.model.*
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.contact.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.contact.Blocked
import chat.sphinx.wrapper.contact.isTrue
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.message.MessagePagination
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class NetworkQueryContactImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryContact()
{
    companion object {
        private const val ENDPOINT_HAS_ADMIN = "/has_admin"
        private const val ENDPOINT_PRODUCTION_CONFIG = "https://config.config.sphinx.chat/api/config/bitcoin"
        private const val ENDPOINT_TEST_CONFIG = "https://config.config.sphinx.chat/api/config/regtest"
        private const val ENDPOINT_ROUTE = "/api/route?pubkey=%s&msat=%s"

        private const val ENDPOINT_GET_NODES = "/api/node"
        private const val PROTOCOL_HTTPS = "https://"
    }

    override fun hasAdmin(
        url: RelayUrl
    ): Flow<LoadResponse<Any, ResponseError>> =
        networkRelayCall.get(
            url = "${url.value}$ENDPOINT_HAS_ADMIN",
            responseJsonSerializer = HasAdminRelayResponse.serializer(),
        )

    override fun getAccountConfig(isProductionEnvironment: Boolean): Flow<LoadResponse<AccountConfigV2Response, ResponseError>> =
        networkRelayCall.get(
            if (isProductionEnvironment) ENDPOINT_PRODUCTION_CONFIG else ENDPOINT_TEST_CONFIG,
            responseJsonSerializer = AccountConfigV2Response.serializer()
        )

    override fun getNodes(routerUrl: String): Flow<LoadResponse<String, ResponseError>> =
        networkRelayCall.getRawJson(
            url = PROTOCOL_HTTPS +  routerUrl + ENDPOINT_GET_NODES
        )

    override fun getRoutingNodes(
        routerUrl: String,
        lightningNodePubKey: LightningNodePubKey,
        milliSats: Long
    ): Flow<LoadResponse<String, ResponseError>> {
        val url = PROTOCOL_HTTPS + routerUrl + ENDPOINT_ROUTE.format(lightningNodePubKey.value, milliSats)

        return networkRelayCall.getRawJson(
            url = url
        )
    }

}