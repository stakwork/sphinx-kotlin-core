package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NewSentStatus(
    val tag: String? = null,
    val status: String? = null,
    val preimage: String? = null,
    val payment_hash: String? = null,
    val message: String? = null
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
