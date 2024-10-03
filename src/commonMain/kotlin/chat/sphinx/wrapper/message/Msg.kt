package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.mqtt.Message.Companion.toMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.IllegalArgumentException

@Serializable
data class Msg(
    val content: String? = null,
    val amount: Long? = null,
    val mediaToken: String? = null,
    val mediaKey: String? = null,
    val mediaType: String? = null,
    val replyUuid: String? = null,
    val threadUuid: String? = null,
    val originalUuid: String? = null,
    val date: Long? = null,
    val invoice: String? = null,
    val paymentHash: String? = null
) {
    companion object {
        @Throws(Exception::class)
        fun String.toMsg(): Msg {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for Message")
        }
    }
}

