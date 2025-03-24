package chat.sphinx.concepts.network.query.message.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MessageMetadata(
    val tz: String
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        @Throws(Exception::class, IllegalArgumentException::class)
        fun String.toMessageMetadata(): MessageMetadata {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for NewCreateTribe")
        }
    }
}
