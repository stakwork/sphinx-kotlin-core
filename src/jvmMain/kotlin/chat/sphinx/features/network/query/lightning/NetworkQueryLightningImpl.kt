package chat.sphinx.features.network.query.lightning

import chat.sphinx.concepts.network.query.lightning.NetworkQueryLightning
import chat.sphinx.concepts.network.query.lightning.model.balance.BalanceAllDto
import chat.sphinx.concepts.network.query.lightning.model.balance.BalanceDto
import chat.sphinx.concepts.network.query.lightning.model.channel.ChannelsDto
import chat.sphinx.concepts.network.query.lightning.model.invoice.*
import chat.sphinx.concepts.network.query.lightning.model.lightning.ActiveLsatDto
import chat.sphinx.concepts.network.query.lightning.model.route.RouteSuccessProbabilityDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.lightning.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.LightningRouteHint
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

class NetworkQueryLightningImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryLightning() {

    companion object {
        private const val ENDPOINT_INVOICES = "/invoices"
        private const val ENDPOINT_INVOICES_CANCEL = "$ENDPOINT_INVOICES/cancel"
        private const val ENDPOINT_CHANNELS = "/channels"
        private const val ENDPOINT_BALANCE = "/balance"
        private const val ENDPOINT_BALANCE_ALL = "$ENDPOINT_BALANCE/all"
        private const val ENDPOINT_ROUTE = "/route"
        private const val ENDPOINT_ROUTE_2 = "/route2"
        private const val ENDPOINT_GET_INFO = "/getinfo"
        private const val ENDPOINT_GET_LSAT = "/active_lsat"
        private const val ENDPOINT_LOGS = "/logs"
        private const val ENDPOINT_INFO = "/info"
        private const val ENDPOINT_QUERY_ONCHAIN_ADDRESS = "/query/onchain_address"
        private const val ENDPOINT_UTXOS = "/utxos"
    }

    ///////////
    /// GET ///
    ///////////
    private val getInvoicesFlowNullData: Flow<LoadResponse<InvoicesDto, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonSerializer = GetInvoicesRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_INVOICES,
            relayData = null
        )
    }

    override fun getInvoices(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<InvoicesDto, ResponseError>> =
        if (relayData == null) {
            getInvoicesFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonSerializer = GetInvoicesRelayResponse.serializer(),
                relayEndpoint = ENDPOINT_INVOICES,
                relayData = relayData
            )
        }

    private val getChannelsFlowNullData: Flow<LoadResponse<ChannelsDto, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonSerializer = GetChannelsRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_CHANNELS,
            relayData = null
        )
    }

    override fun getChannels(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChannelsDto, ResponseError>> =
        if (relayData == null) {
            getChannelsFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonSerializer = GetChannelsRelayResponse.serializer(),
                relayEndpoint = ENDPOINT_CHANNELS,
                relayData = relayData
            )
        }

    private val getBalanceFlowNullData: Flow<LoadResponse<BalanceDto, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonSerializer = GetBalanceRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_BALANCE,
            relayData = null
        )
    }

    override fun getBalance(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<BalanceDto, ResponseError>> =
        if (relayData == null) {
            getBalanceFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonSerializer = GetBalanceRelayResponse.serializer(),
                relayEndpoint = ENDPOINT_BALANCE,
                relayData = relayData
            )
        }

    private val getBalanceAllFlowNullData: Flow<LoadResponse<BalanceAllDto, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonSerializer = GetBalanceAllRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_BALANCE_ALL,
            relayData = null
        )
    }

    override fun getBalanceAll(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<BalanceAllDto, ResponseError>> =
        if (relayData == null) {
            getBalanceAllFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonSerializer = GetBalanceAllRelayResponse.serializer(),
                relayEndpoint = ENDPOINT_BALANCE_ALL,
                relayData = relayData
            )
        }

    override fun checkRoute(
        publicKey: LightningNodePubKey,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<RouteSuccessProbabilityDto, ResponseError>> =
        checkRouteImpl(
            endpoint = ENDPOINT_ROUTE + "?pubkey=${publicKey.value}&route_hint=",
            relayData = relayData
        )

    override fun checkRoute(
        publicKey: LightningNodePubKey,
        routeHint: LightningRouteHint,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<RouteSuccessProbabilityDto, ResponseError>> =
        checkRouteImpl(
            endpoint = ENDPOINT_ROUTE + "?pubkey=${publicKey.value}&route_hint=${routeHint.value}",
            relayData = relayData
        )

    override fun checkRoute(
        chatId: ChatId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<RouteSuccessProbabilityDto, ResponseError>> =
        checkRouteImpl(
            endpoint = ENDPOINT_ROUTE_2 + "?chat_id=${chatId.value}",
            relayData = relayData
        )

    private fun checkRouteImpl(
        endpoint: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<RouteSuccessProbabilityDto, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonSerializer = CheckRouteRelayResponse.serializer(),
            relayEndpoint = endpoint,
            relayData = relayData
        )

    override fun getLogs(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<String, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonSerializer = GetLogsRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_LOGS,
            relayData = relayData,
        )

    override fun postRequestPayment(
        postPaymentDto: PostRequestPaymentDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<LightningPaymentInvoiceDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = PostInvoicePaymentRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_INVOICES,
            requestBodyPair = Pair(
                postPaymentDto,
                PostRequestPaymentDto.serializer()
            ),
            relayData = relayData
        )

    override fun putLightningPaymentRequest(
        payRequestDto: PayRequestDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<PaymentMessageDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonSerializer = PayLightningPaymentRequestRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_INVOICES,
            requestBodyPair = Pair(
                payRequestDto,
                PayRequestDto.serializer()
            ),
            relayData = relayData
        )

    override fun getActiveLSat(
        issuer: String?,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ActiveLsatDto, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonSerializer = GetActiveLSatRelayResponse.serializer(),
            relayEndpoint = if (issuer != null) "$ENDPOINT_GET_LSAT?issuer=${issuer!!}" else ENDPOINT_GET_LSAT,
            relayData = relayData,
        )
    
//    app.get('/getinfo', details.getInfo)
//    app.get('/info', details.getNodeInfo)
//    app.get('/route', details.checkRoute)
//    app.get('/query/onchain_address/:app', queries.queryOnchainAddress)
//    app.get('/utxos', queries.listUTXOs)

    ////////////
    /// POST ///
    ////////////
//    app.post('/invoices', invoices.createInvoice)
//    app.post('/invoices/cancel', invoices.cancelInvoice)
//    app.post('/payment', payments.sendPayment)

    //////////////
    /// DELETE ///
    //////////////
}