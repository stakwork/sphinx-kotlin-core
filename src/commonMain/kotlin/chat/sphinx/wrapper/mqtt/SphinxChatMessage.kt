package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class Message(
    val content: String?,
    val amount: Int?,
    val mediaToken: String?,
    val mediaKey: String?,
    val mediaType: String?,
    val replyUuid: String?,
    val threadUuid: String?,
    val member: String?,
    val invoice: String?
) {
    @Throws(AssertionError::class)
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun String.toMessageNull(): Message? {
            return try {
                this.toMessage()
            } catch (e: Exception) {
                null
            }
        }

        @Throws(Exception::class)
        fun String.toMessage(): Message {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for Message")
        }
    }
}
