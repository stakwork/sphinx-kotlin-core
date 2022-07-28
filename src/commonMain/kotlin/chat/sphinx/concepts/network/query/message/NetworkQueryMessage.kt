package chat.sphinx.concepts.network.query.message

import chat.sphinx.concepts.network.query.message.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.MessageId
import chat.sphinx.wrapper.message.MessagePagination
import chat.sphinx.wrapper.message.MessageType
import chat.sphinx.wrapper.message.MessageUUID
import chat.sphinx.wrapper.message.media.MediaToken
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryMessage {

    abstract fun getMessages(
        messagePagination: MessagePagination?,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<GetMessagesResponse, ResponseError>>

    abstract fun getPayments(
        offset: Int = 0,
        limit: Int = 50,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<List<TransactionDto>, ResponseError>>

    abstract fun sendMessage(
        postMessageDto: PostMessageDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun boostMessage(
        boostMessageDto: PostBoostMessageDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun sendPayment(
        postPaymentDto: PostPaymentDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun sendPaymentRequest(
        postPaymentRequestDto: PostPaymentRequestDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun payPaymentRequest(
        putPaymentRequestDto: PutPaymentRequestDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun sendKeySendPayment(
        postPaymentDto: PostPaymentDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<KeySendPaymentDto, ResponseError>>

    abstract fun payAttachment(
        chatId: ChatId,
        contactId: ContactId?,
        amount: Sat,
        mediaToken: MediaToken,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun readMessages(
        chatId: ChatId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<Any?, ResponseError>>

//    app.post('/messages/clear', messages.clearMessages)

    //////////////
    /// DELETE ///
    //////////////
    /**
     * Delete message with the associated [MessageId]
     *
     * DELETE /message/$messageId
     */
    abstract fun deleteMessage(
        messageId: MessageId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MessageDto, ResponseError>>

    abstract fun processMemberRequest(
        contactId: ContactId,
        messageId: MessageId,
        type: MessageType,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<PutMemberResponseDto, ResponseError>>
}
