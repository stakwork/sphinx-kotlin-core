package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MessageTag(
    val tag: String? = null,
    val ts: Long? = null,
    val status: String? = null,
    val error: String? = null
)

@Serializable
data class TagMessageList(
    val tags: List<MessageTag>
) {
    companion object {
        fun String.toTagsList(): List<MessageTag>? {
            return try {
                Json.decodeFromString<List<MessageTag>>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
