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
    val end_number: Int?,
    val end_date: String?,
    val count: Int,
    val ended: @Polymorphic Any?,
    val paused: @Polymorphic Any?,
    val created_at: String,
    val updated_at: String,
    val interval: String,
    val next: String,
    val chat: chat.sphinx.concepts.network.query.chat.model.ChatDto?,
) {
    @Transient
    val endedActual: Boolean =
        when (ended) {
            is Boolean -> {
                ended
            }
            is Double -> {
                ended.toInt() == 1
            }
            else -> {
                true
            }
        }

    @Transient
    val pausedActual: Boolean =
        when (paused) {
            is Boolean -> {
                paused
            }
            is Double -> {
                paused.toInt() == 1
            }
            else -> {
                true
            }
        }
}
