package chat.sphinx.concepts.network.query.subscription.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SubscriptionDto(
    val id: Long,
    val chat_id: Long,
    val contact_id: Long,
    val cron: String,
    val amount: Long,
    val total_paid: Long,
    val end_number: Int? = null,
    val end_date: String? = null,
    val count: Int,
    val ended: Boolean? = null,
    val paused: Boolean? = null,
    val created_at: String,
    val updated_at: String,
    val interval: String,
    val next: String,
    val chat: ChatDto? = null,
) {
    @Transient
    val endedActual: Boolean =
        ended ?: false

    @Transient
    val pausedActual: Boolean =
        paused ?: false
}
