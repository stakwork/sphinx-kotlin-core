package chat.sphinx.features.socket_io.json


import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.query.invite.model.InviteDto
import chat.sphinx.concepts.network.query.lightning.model.invoice.LightningPaymentInvoiceDto
import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.socket_io.GroupDto
import kotlinx.io.errors.IOException
import kotlinx.serialization.Serializable


@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun<T: Any, V: MessageResponse<T>> Moshi.getMessageResponse(
    adapter: Class<V>,
    json: String
): T {
    val jsonResolved: String = if (
        adapter == MessageResponse.ResponseGroup::class.java &&
        json.contains("\"contact\":{}")
    ) {
        json.replace("\"contact\":{}", "\"contact\":null")
    } else {
        json
    }

    return adapter(adapter)
        .fromJson(jsonResolved)
        ?.response
        ?: throw JsonDataException("Failed to convert SocketIO Message.response Json to ${adapter.simpleName}")
}

@Serializable
internal data class GroupDtoImpl(
    override val chat: ChatDto,
    override val contact: ContactDto?,
    override val message: MessageDto
): GroupDto()

internal sealed class MessageResponse<T> {
    abstract val response: T

    @Serializable
    internal class ResponseChat(override val response: ChatDto): MessageResponse<ChatDto>()

    @Serializable
    internal class ResponseContact(override val response: ContactDto): MessageResponse<ContactDto>()

    @Serializable
    internal class ResponseGroup(override val response: GroupDtoImpl): MessageResponse<GroupDtoImpl>()

    @Serializable
    internal class ResponseInvite(override val response: InviteDto): MessageResponse<InviteDto>()

    @Serializable
    internal class ResponseInvoice(override val response: LightningPaymentInvoiceDto): MessageResponse<LightningPaymentInvoiceDto>()

    @Serializable
    internal class ResponseMessage(override val response: MessageDto): MessageResponse<MessageDto>()
}

