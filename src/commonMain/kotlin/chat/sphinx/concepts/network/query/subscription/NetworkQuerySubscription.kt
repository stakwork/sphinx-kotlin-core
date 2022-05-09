package chat.sphinx.concepts.network.query.subscription

import chat.sphinx.concepts.network.query.subscription.model.PostSubscriptionDto
import chat.sphinx.concepts.network.query.subscription.model.PutSubscriptionDto
import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.TransportToken
import chat.sphinx.wrapper.subscription.SubscriptionId
import kotlinx.coroutines.flow.Flow

abstract class NetworkQuerySubscription {

    ///////////
    /// GET ///
    ///////////
    abstract fun getSubscriptions(
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<List<SubscriptionDto>, ResponseError>>

    abstract fun getSubscriptionById(
        subscriptionId: SubscriptionId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>>

    abstract fun getSubscriptionsByContactId(
        contactId: ContactId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<List<SubscriptionDto>, ResponseError>>

    ///////////
    /// PUT ///
    ///////////
    abstract fun putSubscription(
        subscriptionId: SubscriptionId,
        putSubscriptionDto: PutSubscriptionDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>>

    abstract fun putPauseSubscription(
        subscriptionId: SubscriptionId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>>

    abstract fun putRestartSubscription(
        subscriptionId: SubscriptionId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>>

    ////////////
    /// POST ///
    ////////////
//    app.post('/subscriptions', subcriptions.createSubscription)
    abstract fun postSubscription(
        postSubscriptionDto: PostSubscriptionDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<SubscriptionDto, ResponseError>>

    //////////////
    /// DELETE ///
    //////////////
    abstract fun deleteSubscription(
        subscriptionId: SubscriptionId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>
}