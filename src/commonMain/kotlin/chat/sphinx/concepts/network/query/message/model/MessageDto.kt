package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.serialization.SphinxBoolean
import chat.sphinx.utils.platform.getFileSystem
import chat.sphinx.wrapper.lightning.asFormattedString
import chat.sphinx.wrapper.lightning.toSat
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.media.FileName
import chat.sphinx.wrapper.message.media.MediaType
import chat.sphinx.wrapper.message.media.toMediaType
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import okio.Path
import kotlin.jvm.Volatile

@Serializable
data class MessageDto(
    val id: Long,
    val uuid: String? = null,
    val chat_id: Long? = null,
    val type: Int,
    val sender: Long,
    val receiver: Long? = null,
    val amount: Long,
    val amount_msat: Long,
    val payment_hash: String? = null,
    val payment_request: String? = null,
    val date: String,
    val expiration_date: String? = null,
    val message_content: String? = null,
    val remote_message_content: String? = null,
    val status: Int? = null,
    val parent_id: Long? = null,
    val subscription_id: Long? = null,
    val media_key: String? = null,
    val media_type: String? = null,
    val media_token: String? = null,
    val seen: SphinxBoolean,
    val created_at: String,
    val updated_at: String,
    val sender_alias: String? = null,
    val sender_pic: String? = null,
    val original_muid: String? = null,
    val reply_uuid: String? = null,
    val network_type: Int? = null,
    val chat: ChatDto? = null,
    val contact: ContactDto? = null,
    val recipient_alias: String? = null,
    val recipient_pic: String? = null,
    val push: SphinxBoolean? = null,
    val person: String? = null,
    val thread_uuid: String? = null

) {
    @Transient
    val seenActual: Boolean = seen.value

    @Transient
    val pushActual: Boolean = push?.value ?: false

    @Transient
    @Volatile
    var messageContentDecrypted: String? = null
        private set

    fun setMessageContentDecrypted(value: String) {
        if (value.isEmpty()) return
        messageContentDecrypted = value
    }

    @Transient
    @Volatile
    var mediaKeyDecrypted: String? = null
        private set

    fun setMediaKeyDecrypted(value: String) {
        if (value.isEmpty()) return
        mediaKeyDecrypted = value
    }

    @Transient
    @Volatile
    var mediaLocalFile: Path? = null
        private set

    @Transient
    @Volatile
    var localFileName: FileName? = null
        private set

    fun setLocalFileName(fileName: FileName) {
        localFileName = fileName
    }

    fun setMediaLocalFile(file: Path) {
        mediaLocalFile = try {
            val fileExists = getFileSystem().exists(file)
            val isFile = getFileSystem().listOrNull(file) == null
            if (getFileSystem().exists(file) && isFile) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getNotificationText(): String? {
        return when {
            this.status == MessageStatus.DELETED -> {
                "Message deleted"
            }
            this.type == MessageType.PAYMENT -> {
                val amount: String = this.amount.toSat()?.asFormattedString(separator = ',', appendUnit = true) ?: "0"
                "Payment Received: $amount"
            }
            this.type.toMessageType().isGroupJoin() -> {
                "Has joined the tribe"
            }
            this.type.toMessageType().isGroupLeave() -> {
                "Just left the tribe"
            }
            this.type.toMessageType().isMemberRequest() -> {
                "Wants to join the tribe"
            }
            this.type.toMessageType().isMemberReject() -> {
                "The admin declined your request"
            }
            this.type.toMessageType().isMemberApprove() -> {
                "Welcome! You’re now a member"
            }
            this.type.toMessageType().isGroupKick() -> {
                "The admin has removed you from this group"
            }
            this.type.toMessageType().isTribeDelete() -> {
                "The admin deleted this tribe"
            }
            this.type.toMessageType().isBoost() -> {
                "Boost received"
            }
            this.type.toMessageType().isMessage() -> {
                this.messageContentDecrypted?.let { decrypted ->
                    decrypted
                } ?: null
            }
            this.type.toMessageType().isInvoice() -> {
                "Invoice received"

            }
            this.type.toMessageType().isDirectPayment() -> {
                "Payment received"
            }
            this.type.toMessageType().isAttachment() -> {
                this.media_type?.toMediaType()?.let { type ->
                    when (type) {
                        is MediaType.Audio -> {
                            "Audio clip"
                        }
                        is MediaType.Image -> {
                            if (type.isGif) {
                                "Gif"
                            } else {
                                "Image"
                            }
                        }
                        is MediaType.Pdf -> {
                            "Pdf"
                        }
                        is MediaType.Text -> {
                            "Paid message"
                        }
                        is MediaType.Unknown -> {
                            "Attachment"
                        }
                        is MediaType.Video -> {
                            "Video"
                        }
                    }?.let { element ->
                        "$element received"
                    }
                } ?: ""
            }
            this.type.toMessageType().isBotRes() -> {
                "Bot response received"
            }
            else -> {
                null
            }
        }
    }
}
