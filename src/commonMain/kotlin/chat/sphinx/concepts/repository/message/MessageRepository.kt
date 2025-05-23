package chat.sphinx.concepts.repository.message

//import androidx.paging.PagingData
//import androidx.paging.PagingSource
import androidx.paging.PagingData
import chat.sphinx.concepts.network.query.message.model.PutPaymentRequestDto
import chat.sphinx.concepts.repository.message.model.SendMessage
import chat.sphinx.concepts.repository.message.model.SendPayment
import chat.sphinx.concepts.repository.message.model.SendPaymentRequest
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.lightning.*
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.payment.PaymentTemplate
import chat.sphinx.wrapper_message.ThreadUUID
import kotlinx.coroutines.flow.Flow
import okio.Path

interface MessageRepository {
    suspend fun getAllMessagesToShowByChatIdPaginated(chatId: ChatId): Flow<PagingData<Message>>
    fun getAllMessagesToShowByChatId(chatId: ChatId, limit: Long, chatThreadUUID: ThreadUUID? = null): Flow<List<Message>>
    fun getMessageById(messageId: MessageId): Flow<Message?>
    fun getMessagesByIds(messagesIds: List<MessageId>): Flow<List<Message?>>
    fun messageGetOkKeysByChatId(chatId: ChatId): Flow<List<MessageId>>
    fun getMessagesByPaymentHashes(paymentHashes: List<LightningPaymentHash>): Flow<List<Message?>>
    fun getTribeLastMemberRequestByContactId(contactId: ContactId, chatId: ChatId, ): Flow<Message?>
    fun getMessageByUUID(messageUUID: MessageUUID): Flow<Message?>
    fun getPaymentsTotalFor(feedId: FeedId): Flow<Sat?>
    fun getSentConfirmedMessagesByChatId(chatId: ChatId): Flow<List<Message>>
    fun getThreadUUIDMessagesByChatId(chatId: ChatId): Flow<List<Message>>
    fun getThreadUUIDMessagesByUUID(chatId: ChatId, threadUUID: ThreadUUID): Flow<List<Message>>

    suspend fun getAllMessagesByUUID(messageUUIDs: List<MessageUUID>): List<Message>

    fun updateMessageContentDecrypted(messageId: MessageId, messageContentDecrypted: MessageContentDecrypted)

    fun readMessages(chatId: ChatId)

    fun sendMessage(sendMessage: SendMessage?)

    suspend fun payAttachment(message: Message)

    fun resendMessage(
        message: Message,
        chat: Chat,
    )

    fun flagMessage(
        message: Message,
        chat: Chat,
    )

    fun sendBoost(
        chatId: ChatId,
        boost: FeedBoost
    )

    suspend fun deleteMessage(message: Message)

    suspend fun deleteAllMessagesAndPubKey(pubKey: String, chatId: ChatId)

    suspend fun getPaymentTemplates() : Response<List<PaymentTemplate>, ResponseError>

    suspend fun sendPayment(
        sendPayment: SendPayment?
    ): Response<Any, ResponseError>

    suspend fun sendPaymentRequest(
        requestPayment: SendPaymentRequest
    ): Response<Any, ResponseError>

    suspend fun payPaymentRequest(putPaymentRequestDto: PutPaymentRequestDto) : Flow<LoadResponse<Any, ResponseError>>

    suspend fun payPaymentRequest(message: Message) : Response<Any, ResponseError>

    suspend fun boostMessage(
        chatId: ChatId,
        pricePerMessage: Sat,
        escrowAmount: Sat,
        messageUUID: MessageUUID,
    ): Response<Any, ResponseError>

    suspend fun sendTribePayment(
        chatId: ChatId,
        amount: Sat,
        messageUUID: MessageUUID,
        text: String,
    )

    fun processMemberRequest(
        chatId: ChatId,
        messageUuid: MessageUUID?,
        memberPubKey: LightningNodePubKey?,
        type: MessageType.GroupAction,
        alias: SenderAlias?,
    )

    suspend fun messageMediaUpdateLocalFile(message: Message, filepath: Path)

    suspend fun upsertMqttMessage(
        msg: Msg,
        msgSender: MsgSender,
        contactTribePubKey: String,
        msgType: MessageType,
        msgUuid: MessageUUID,
        msgIndex: MessageId,
        msgAmount: Sat?,
        originalUuid: MessageUUID?,
        timestamp: DateTime?,
        date: DateTime?,
        fromMe: Boolean,
        realPaymentAmount: Sat?,
        paymentRequest: LightningPaymentRequest?,
        paymentHash: LightningPaymentHash?,
        bolt11: Bolt11?,
        tag: TagMessage?,
        isRestore: Boolean
    )

    suspend fun deleteMqttMessage(messageUuid: MessageUUID)

    fun getMaxIdMessage(): Flow<Long?>
    fun getLastMessage(): Flow<Message?>
    fun getTribeLastMemberRequestBySenderAlias(alias: SenderAlias, chatId: ChatId): Flow<Message?>

    fun sendMediaKeyOnPaidPurchase(
        msg: Msg,
        contactInfo: MsgSender,
        paidAmount: Sat
    )

    suspend fun sendNewPaymentRequest(
        requestPayment: SendPayment
    )
}
