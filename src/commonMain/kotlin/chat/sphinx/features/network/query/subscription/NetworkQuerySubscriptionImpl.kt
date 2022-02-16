package chat.sphinx.features.network.query.subscription

import chat.sphinx.concepts.network.query.subscription.NetworkQuerySubscription
import chat.sphinx.concepts.network.query.subscription.model.PostSubscriptionDto
import chat.sphinx.concepts.network.query.subscription.model.PutSubscriptionDto
import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.subscription.model.DeleteSubscriptionRelayResponse
import chat.sphinx.features.network.query.subscription.model.GetSubscriptionsRelayResponse
import chat.sphinx.features.network.query.subscription.model.SubscriptionRelayResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.subscription.SubscriptionId
import kotlinx.coroutines.flow.Flow

class NetworkQuerySubscriptionImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQuerySubscription() {

    companion object {
        private const val ENDPOINT_SUBSCRIPTION = "/subscription"
        private const val ENDPOINT_SUBSCRIPTIONS = "/subscriptions"
    }

    ///////////
    /// GET ///
    ///////////
    private val getSubscriptionsFlowNullData: Flow<LoadResponse<List<SubscriptionDto>, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonClass = GetSubscriptionsRelayResponse::class.java,
            relayEndpoint = ENDPOINT_SUBSCRIPTIONS,
            relayData = null
        )
    }

    override fun getSubscriptions(
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<List<SubscriptionDto>, ResponseError>> =
        if (relayData == null) {
            getSubscriptionsFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonClass = GetSubscriptionsRelayResponse::class.java,
                relayEndpoint = ENDPOINT_SUBSCRIPTIONS,
                relayData = relayData
            )
        }

    override fun getSubscriptionById(
        subscriptionId: SubscriptionId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonClass = SubscriptionRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTION/${subscriptionId.value}",
            relayData = relayData
        )

    override fun getSubscriptionsByContactId(
        contactId: ContactId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<List<SubscriptionDto>, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonClass = GetSubscriptionsRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTIONS/contact/${contactId.value}",
            relayData = relayData
        )

    ///////////
    /// PUT ///
    ///////////
    override fun putSubscription(
        subscriptionId: SubscriptionId,
        putSubscriptionDto: PutSubscriptionDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonClass = SubscriptionRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTION/${subscriptionId.value}",
            requestBodyJsonClass = PutSubscriptionDto::class.java,
            requestBody = putSubscriptionDto,
            relayData = relayData
        )

    override fun putPauseSubscription(
        subscriptionId: SubscriptionId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonClass = SubscriptionRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTION/${subscriptionId.value}/pause",
            requestBodyJsonClass = Map::class.java,
            requestBody = mapOf(Pair("", "")),
            relayData = relayData
        )

    override fun putRestartSubscription(
        subscriptionId: SubscriptionId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonClass = SubscriptionRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTION/${subscriptionId.value}/restart",
            requestBodyJsonClass = Map::class.java,
            requestBody = mapOf(Pair("", "")),
            relayData = relayData
        )

    ////////////
    /// POST ///
    ////////////
    override fun postSubscription(
        postSubscriptionDto: PostSubscriptionDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonClass = SubscriptionRelayResponse::class.java,
            relayEndpoint = ENDPOINT_SUBSCRIPTIONS,
            requestBodyJsonClass = PostSubscriptionDto::class.java,
            requestBody = postSubscriptionDto,
            relayData = relayData
        )

    //////////////
    /// DELETE ///
    //////////////
    override fun deleteSubscription(
        subscriptionId: SubscriptionId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<Any, ResponseError>> =
        networkRelayCall.relayDelete(
            responseJsonClass = DeleteSubscriptionRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_SUBSCRIPTION/${subscriptionId.value}",
            requestBody = null,
            relayData = relayData
        )
}
