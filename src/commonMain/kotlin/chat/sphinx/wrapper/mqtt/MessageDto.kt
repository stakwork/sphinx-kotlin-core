package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

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
    val seen: Boolean? = null,
    val created_at: String,
    val updated_at: String,
    val sender_alias: String? = null,
    val sender_pic: String? = null,
    val original_muid: String? = null,
    val reply_uuid: String? = null,
    val network_type: Int? = null,
    val chat: Long? = null,
    val contact: Long? = null,
    val recipient_alias: String? = null,
    val recipient_pic: String? = null,
    val push: Boolean? = null,
    val person: String? = null,
    val thread_uuid: String? = null,
    val error_message: String? = null,
    val tag_message: String? = null
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
