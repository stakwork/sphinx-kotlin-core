package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MessagesFetchRequest(
    val since: Long,
    val limit: Int
)

@Throws(AssertionError::class)
fun MessagesFetchRequest.toJson(): String {
    return Json.encodeToString(MessagesFetchRequest.serializer(), this)
}

fun String.toMessagesFetchRequestOrNull(): MessagesFetchRequest? {
    return try {
        this.toMessagesFetchRequest()
    } catch (e: Exception) {
        null
    }
}

@Throws(Exception::class)
fun String.toMessagesFetchRequest(): MessagesFetchRequest {
    return Json.decodeFromString(MessagesFetchRequest.serializer(), this)
}
