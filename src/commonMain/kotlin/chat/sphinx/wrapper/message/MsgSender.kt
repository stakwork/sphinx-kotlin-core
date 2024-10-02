package chat.sphinx.wrapper.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.IllegalArgumentException

@Serializable
data class MsgSender(
    val pubkey: String,
    val route_hint: String?,
    val alias: String?,
    val photo_url: String?,
    val person: String?,
    val confirmed: Boolean,
    val code: String?,
    val host: String?,
    val role: Int?
) {
    companion object {
        fun String.toMsgSenderNull(): MsgSender? {
            return try {
                this.toMsgSender()
            } catch (e: Exception) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        @Throws(Exception::class)
        fun String.toMsgSender(): MsgSender {
            if (this.isEmpty()) {
                throw IllegalArgumentException("Empty JSON for MsgSender")
            }
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for MsgSender")
        }
    }
}
