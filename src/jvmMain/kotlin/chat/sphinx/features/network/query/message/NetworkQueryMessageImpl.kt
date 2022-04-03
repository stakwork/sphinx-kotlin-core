package chat.sphinx.features.network.query.message

import chat.sphinx.concepts.network.query.message.NetworkQueryMessage
import chat.sphinx.concepts.network.query.message.model.*
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.message.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.media.MediaToken
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.PolymorphicSerializer

class NetworkQueryMessageImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryMessage() {

    companion object {
        private const val ENDPOINT_ATTACHMENT = "/attachment"

        private const val ENDPOINT_MSGS = "/msgs"
        private const val ENDPOINT_MESSAGE = "/message"
        private const val ENDPOINT_DELETE_MESSAGE = "/message/%d"
        private const val ENDPOINT_MEMBER_APPROVED = "/member/%d/approved/%d"
        private const val ENDPOINT_MEMBER_REJECTED = "/member/%d/rejected/%d"
        private const val ENDPOINT_MESSAGES_READ = "/messages/%d/read"
        private const val ENDPOINT_MESSAGES = "${ENDPOINT_MESSAGE}s"
        private const val ENDPOINT_PAYMENT = "/payment"
        private const val ENDPOINT_PAYMENTS = "${ENDPOINT_PAYMENT}s"
        private const val ENDPOINT_PAY_ATTACHMENT = "/purchase"
        private const val ENDPOINT_INVOICES = "/invoices"
    }

    override fun getMessages(
        messagePagination: MessagePagination?,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<GetMessagesResponse, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonSerializer = GetMessagesRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_MSGS + (messagePagination?.value ?: "") + "&order=desc",
            relayData = relayData
        )

    override fun getPayments(
        offset: Int,
        limit: Int,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<List<TransactionDto>, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonSerializer = GetPaymentsRelayResponse.serializer(),
            relayEndpoint = "$ENDPOINT_PAYMENTS?offset=$offset&limit=$limit",
            relayData = relayData
        )

    override fun sendMessage(
        postMessageDto: PostMessageDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = if (postMessageDto.media_key_map != null) {
                ENDPOINT_ATTACHMENT
            } else {
                ENDPOINT_MESSAGES
            },
            requestBodyPair = Pair(
                postMessageDto,
                PostMessageDto.serializer()
            ),
            relayData = relayData
        )

    override fun sendPayment(
        postPaymentDto: PostPaymentDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_PAYMENT,
            requestBodyPair = Pair(
                postPaymentDto,
                PostPaymentDto.serializer()
            ),
            relayData = relayData
        )

    override fun sendPaymentRequest(
        postPaymentRequestDto: PostPaymentRequestDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_INVOICES,
            requestBodyPair = Pair(
                postPaymentRequestDto,
                PostPaymentRequestDto.serializer()
            ),
            relayData = relayData
        )

    override fun payPaymentRequest(
        putPaymentRequestDto: PutPaymentRequestDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_INVOICES,
            requestBodyPair = Pair(
                putPaymentRequestDto,
                PutPaymentRequestDto.serializer()
            ),
            relayData = relayData
        )

    override fun sendKeySendPayment(
        postPaymentDto: PostPaymentDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<KeySendPaymentDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = KeySendPaymentRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_PAYMENT,
            requestBodyPair = Pair(
                postPaymentDto,
                PostPaymentDto.serializer()
            ),
            relayData = relayData
        )

    override fun boostMessage(
        chatId: ChatId,
        pricePerMessage: Sat,
        escrowAmount: Sat,
        tipAmount: Sat,
        messageUUID: MessageUUID,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> {
        val postBoostMessageDto: PostBoostMessage = try {
            PostBoostMessage(
                chat_id = chatId.value,
                amount = pricePerMessage.value + escrowAmount.value + tipAmount.value,
                message_price = pricePerMessage.value + escrowAmount.value,
                reply_uuid = messageUUID.value
            )
        } catch (e: IllegalArgumentException) {
            return flowOf(Response.Error(ResponseError("Incorrect Arguments provided", e)))
        }

        return networkRelayCall.relayPost(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_MESSAGES,
            requestBodyPair = Pair(
                postBoostMessageDto,
                PostBoostMessage.serializer()
            ),
            relayData = relayData
        )
    }

    override fun payAttachment(
        chatId: ChatId,
        contactId: ContactId?,
        amount: Sat,
        mediaToken: MediaToken,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> {

        val payAttachmentDto = PostPayAttachmentDto(
                chat_id = chatId.value,
                contact_id = contactId?.value,
                amount = amount.value,
                media_token = mediaToken.value
            )

        return networkRelayCall.relayPost(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_PAY_ATTACHMENT,
            requestBodyPair = Pair(
                payAttachmentDto,
                PostPayAttachmentDto.serializer()
            ),
            relayData = relayData
        )
    }


    override fun readMessages(
        chatId: ChatId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<Any?, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = ReadMessagesRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_MESSAGES_READ, chatId.value),
            requestBodyPair = Pair(
                mapOf(Pair("", "")),
                PolymorphicSerializer(Any::class)
            ),
            relayData = relayData
        )

//    app.post('/messages/clear', messages.clearMessages)

    //////////////
    /// DELETE ///
    //////////////
    /**
     * Deletes a message with the id [MessageId]
     *
     * DELETE /message/$messageId
     */
    override fun deleteMessage(
        messageId: MessageId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<MessageDto, ResponseError>> =
        networkRelayCall.relayDelete(
            responseJsonSerializer = MessageRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_DELETE_MESSAGE, messageId.value),
            relayData = relayData
        )


    override fun processMemberRequest(
        contactId: ContactId,
        messageId: MessageId,
        type: MessageType,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<PutMemberResponseDto, ResponseError>> =
        networkRelayCall.relayPut<PutMemberResponseDto, Any, PutMemberRelayResponse>(
            responseJsonSerializer = PutMemberRelayResponse.serializer(),
            relayEndpoint = if (type.isMemberApprove()) {
                String.format(ENDPOINT_MEMBER_APPROVED, contactId.value, messageId.value)
            } else {
                String.format(ENDPOINT_MEMBER_REJECTED, contactId.value, messageId.value)
            },
            requestBodyPair = null,
            relayData = relayData
        )
}
