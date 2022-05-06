package chat.sphinx.features.socket_io.json


import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.query.invite.model.InviteDto
import chat.sphinx.concepts.network.query.lightning.model.invoice.InvoiceDto
import chat.sphinx.concepts.network.query.lightning.model.invoice.LightningPaymentInvoiceDto
import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.socket_io.GroupDto
import chat.sphinx.utils.SphinxJson
import kotlinx.io.errors.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseGroup(): GroupDtoImpl {
    val jsonResolved: String = this.replace("\"contact\":{}", "\"contact\":null")

    return SphinxJson.decodeFromString<MessageResponse.ResponseGroup>(jsonResolved).response
}

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseMessage(): MessageDto = SphinxJson.decodeFromString<MessageResponse.ResponseMessage>(this).response

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseInvite(): InviteDto = SphinxJson.decodeFromString<MessageResponse.ResponseInvite>(this).response

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseInvoice(): LightningPaymentInvoiceDto = SphinxJson.decodeFromString<MessageResponse.ResponseInvoice>(this).response

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseChat(): chat.sphinx.concepts.network.query.chat.model.ChatDto = SphinxJson.decodeFromString<MessageResponse.ResponseChat>(this).response

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageResponseContact(): ContactDto = SphinxJson.decodeFromString<MessageResponse.ResponseContact>(this).response

@Serializable
internal data class GroupDtoImpl(
    override val chat: ChatDto,
    override val contact: ContactDto? = null,
    override val message: MessageDto
): GroupDto()

internal sealed class MessageResponse<T> {
    abstract val response: T

    @Serializable
    internal class ResponseChat(override val response: ChatDto): MessageResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto>()

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

