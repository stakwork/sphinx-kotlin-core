package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.mqtt.Message.Companion.toMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.IllegalArgumentException

@Serializable
data class Msg(
    val content: String?,
    val amount: Long?,
    val mediaToken: String?,
    val mediaKey: String?,
    val mediaType: String?,
    val replyUuid: String?,
    val threadUuid: String?,
    val originalUuid: String?,
    val date: Long?,
    val invoice: String?,
    val paymentHash: String?
) {
    companion object {
        @Throws(Exception::class)
        fun String.toMsg(): Msg {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for Message")
        }
    }
}

