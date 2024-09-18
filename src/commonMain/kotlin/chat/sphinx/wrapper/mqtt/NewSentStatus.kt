package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NewSentStatus(
    val tag: String?,
    val status: String?,
    val preimage: String?,
    val payment_hash: String?,
    val message: String?
) {

    companion object {
        @Throws(Exception::class, IllegalArgumentException::class)
        fun String.toNewSentStatus(): NewSentStatus {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for NewSentStatus")
        }
    }

    fun isFailedMessage(): Boolean {
        return !(status?.contains("COMPLETE") == true || status?.contains("PENDING") == true)
    }
}
