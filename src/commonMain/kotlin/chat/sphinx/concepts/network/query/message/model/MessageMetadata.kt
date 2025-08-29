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
        fun String.toMessageMetadata(): MessageMetadata? =
            runCatching { Json.decodeFromString<MessageMetadata>(this) }.getOrNull()
    }
}

