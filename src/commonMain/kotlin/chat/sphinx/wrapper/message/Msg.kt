package chat.sphinx.wrapper.message

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
    val encryptedDate: String? = null, // Added this line if you need encryptedDate
    val invoice: String? = null,
    val paymentHash: String? = null,
    val encryptedTag: String? = null
) {
    companion object {
        private val jsonDecoder = Json { ignoreUnknownKeys = true }

        @Throws(Exception::class)
        fun String.toMsg(): Msg {
            return jsonDecoder.decodeFromString(this)
                ?: throw IllegalArgumentException("Invalid JSON for Message")
        }
    }
}
