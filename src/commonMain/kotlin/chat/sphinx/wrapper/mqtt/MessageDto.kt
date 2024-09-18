package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

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
    val parent_id: Long?,
    val subscription_id: Long?,
    val media_key: String?,
    val media_type: String?,
    val media_token: String?,
    val seen: Boolean? = null,
    val created_at: String,
    val updated_at: String,
    val sender_alias: String?,
    val sender_pic: String?,
    val original_muid: String?,
    val reply_uuid: String?,
    val network_type: Int?,
    val chat: Long?,
    val contact: Long?,
    val recipient_alias: String?,
    val recipient_pic: String?,
    val push: Boolean? = null,
    val person: String?,
    val thread_uuid: String?,
    val error_message: String?,
    val tag_message: String?
) {
    @Transient
    val seenActual: Boolean = seen ?: false

    @Transient
    val pushActual: Boolean = push ?: false

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
    var mediaLocalFile: File? = null
        private set

    fun setMediaLocalFile(file: File) {
        mediaLocalFile = try {
            if (file.exists() && file.isFile) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
