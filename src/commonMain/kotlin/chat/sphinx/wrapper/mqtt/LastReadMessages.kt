package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LastReadMessages(
    val lastRead: Map<String, Long>
) {
    companion object {
        fun String.toLastReadMap(): Map<String, Long>? {
            return try {
                Json.decodeFromString<Map<String, Long>>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
