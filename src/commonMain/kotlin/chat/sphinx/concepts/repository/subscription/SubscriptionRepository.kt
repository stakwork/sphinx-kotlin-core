package chat.sphinx.concepts.repository.subscription

import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.subscription.EndNumber
import chat.sphinx.wrapper.subscription.Subscription
import chat.sphinx.wrapper.subscription.SubscriptionId
import kotlinx.coroutines.flow.Flow


interface SubscriptionRepository {
    fun getActiveSubscriptionByContactId(
        contactId: ContactId
    ): Flow<Subscription?>

    suspend fun createSubscription(
        amount: Sat,
        interval: String,
        contactId: ContactId,
        chatId: ChatId?,
        endDate: String?,
        endNumber: EndNumber?
    ): Response<Any, ResponseError>

    suspend fun updateSubscription(
        id: SubscriptionId,
        amount: Sat,
        interval: String,
        contactId: ContactId,
        chatId: ChatId?,
        endDate: String?,
        endNumber: EndNumber?
    ): Response<Any, ResponseError>

    suspend fun restartSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError>

    suspend fun pauseSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError>

    suspend fun deleteSubscription(
        subscriptionId: SubscriptionId
    ): Response<Any, ResponseError>
}
