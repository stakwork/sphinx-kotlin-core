package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.utils.platform.getFileSystem
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import okio.Path
import kotlin.jvm.Volatile

@Serializable
data class MessageDto(
    val id: Long,
    val uuid: String?,
    val chat_id: Long?,
    val type: Int,
    val sender: Long,
    val receiver: Long?,
    val amount: Long,
    val amount_msat: Long,
    val payment_hash: String?,
    val payment_request: String?,
    val date: String,
    val expiration_date: String?,
    val message_content: String?,
    val remote_message_content: String?,
    val status: Int?,
//    val status_map: Map<Long, Int?>?, // contact_id : their message's 'status'
    val parent_id: Long?,
    val subscription_id: Long?,
    val media_key: String?,
    val media_type: String?,
    val media_token: String?,
    val seen: @Polymorphic Any,
    val created_at: String,
    val updated_at: String,
    val sender_alias: String?,
    val sender_pic: String?,
    val original_muid: String?,
    val reply_uuid: String?,
    val network_type: Int?,
    val chat: chat.sphinx.concepts.network.query.chat.model.ChatDto?,
    val contact: ContactDto?,
) {
    @Transient
    val seenActual: Boolean =
        when (seen) {
            is Boolean -> {
                seen
            }
            is Double -> {
                seen.toInt() == 1
            }
            else -> {
                false
            }
        }

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

    fun setMediaLocalFile(file: Path) {
        mediaLocalFile = try {
            if (getFileSystem().exists(file) && getFileSystem().listOrNull(file) == null) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
