package chat.sphinx.wrapper.subscription

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.Sat

data class Subscription(
    val id: SubscriptionId,
    val cron: Cron,
    val amount: Sat,
    val endNumber: EndNumber?,
    val count: SubscriptionCount,
    val endDate: DateTime?,
    val ended: Boolean,
    val paused: Boolean,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val chatId: ChatId,
    val contactId: ContactId
)