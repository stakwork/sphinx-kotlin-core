package chat.sphinx.concepts.network.query.subscription.model

import kotlinx.serialization.Serializable

@Serializable
data class PostSubscriptionDto(
    val amount: Long,
    val contact_id: Long,
    val chat_id: Long? = null,
    val end_number: Long? = null,
    val end_date: String? = null,
    val interval: String
)
