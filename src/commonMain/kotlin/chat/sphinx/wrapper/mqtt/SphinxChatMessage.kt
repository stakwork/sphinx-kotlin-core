package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class Message(
    val content: String? = null,
    val amount: Int? = null,
    val mediaToken: String? = null,
    val mediaKey: String? = null,
    val mediaType: String? = null,
    val replyUuid: String? = null,
    val threadUuid: String? = null,
    val member: String? = null,
    val invoice: String? = null,
    val metadata: String? = null
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
