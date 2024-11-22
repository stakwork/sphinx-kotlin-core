package chat.sphinx.wrapper.mqtt

import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.toChatId
import chat.sphinx.wrapper.dashboard.toContactId
import chat.sphinx.wrapper.message.SenderAlias
import chat.sphinx.wrapper.message.toSenderAlias
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: Long,
    val chat_id: Long? = null,
    val type: Int,
    val sender: Long,
    val sender_alias: String? = null,
    val receiver: Long? = null,
    val amount: Long,
    val payment_hash: String? = null,
    val payment_request: String? = null,
    val date: Long? = null,
    val reply_uuid: String? = null,
    val error_message: String? = null,
    val message_content: String? = null
) {

    fun isIncomingWithSender(ownerId: ContactId): Boolean {
        return sender != ownerId.value
    }

    fun isOutgoingWithReceiver(ownerId: ContactId): Boolean {
        return receiver != null && sender == ownerId.value
    }

    fun isOutgoingMessageBoost(ownerId: ContactId): Boolean {
        return reply_uuid != null && sender == ownerId.value
    }

    fun isPaymentInChat(): Boolean {
        return chat_id != null
    }

    fun getSenderId(): ContactId? {
        return sender.toContactId()
    }

    fun getSenderAlias(): SenderAlias? {
        return sender_alias?.toSenderAlias()
    }

    fun getReceiverId(): ContactId? {
        return receiver?.toContactId()
    }

    fun getChatId(): ChatId? {
        return chat_id?.toChatId()
    }

    fun isOutgoingPayment(ownerId: ContactId): Boolean {
        return sender == ownerId.value && error_message.isNullOrEmpty()
    }

    fun isIncomingPayment(ownerId: ContactId): Boolean {
        return sender != ownerId.value
    }

    fun isFailedPayment(): Boolean {
        return error_message?.isNotEmpty() == true
    }

    fun isBountyPayment(): Boolean {
        return (message_content ?: "").isNotEmpty() && sender == -1L && receiver == 0L
    }
}
