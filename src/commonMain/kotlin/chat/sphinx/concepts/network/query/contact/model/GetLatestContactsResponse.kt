package chat.sphinx.concepts.network.query.contact.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.invite.model.InviteDto
import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetLatestContactsResponse(
    val contacts: List<ContactDto>,
    val chats: List<ChatDto>,
    val subscriptions: List<SubscriptionDto>,
    val invites: List<InviteDto>,
)
