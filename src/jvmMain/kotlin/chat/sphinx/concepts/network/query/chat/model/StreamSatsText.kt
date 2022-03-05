package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Throws(AssertionError::class)
fun StreamSatsText.toJson(): String =
    Json.encodeToString(this)

@Serializable
data class StreamSatsText(
    val feedID: String,
    val itemID: String,
    val ts: Long,
    val speed: Double,
    val uuid: String? = null,
)