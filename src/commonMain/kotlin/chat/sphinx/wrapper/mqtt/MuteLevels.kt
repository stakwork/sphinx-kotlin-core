package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MuteLevels(
    val muteLevels: Map<String, Int>
) {
    companion object {
        fun String.toMuteLevelsMap(): Map<String, Int>? {
            return try {
                Json.decodeFromString<Map<String, Int>>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
