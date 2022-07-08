package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.serialization.SphinxBoolean
import chat.sphinx.utils.platform.getFileSystem
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
) {
    @Transient
    val seenActual: Boolean = seen.value

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
}
