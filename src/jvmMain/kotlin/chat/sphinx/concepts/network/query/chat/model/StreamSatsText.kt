package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Encoder

@Throws(AssertionError::class)
fun StreamSatsText.toJson(): String =
    moshi.adapter(StreamSatsText::class.java)
        .toJson(
            StreamSatsText(
                feedID,
                itemID,
                ts,
                speed,
                uuid
            )
        )

@Serializable
data class StreamSatsText(
    val feedID: String,
    val itemID: String,
    val ts: Long,
    val speed: Double,
    val uuid: String? = null,
)