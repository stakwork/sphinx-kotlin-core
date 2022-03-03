package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.message.media.MessageMedia
import kotlinx.io.errors.EOFException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("NOTHING_TO_INLINE")
inline fun String.toGiphyDataOrNull(): GiphyData? =
    try {
        this.toGiphyData()
    } catch (e: Exception) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
@Throws(IllegalArgumentException::class)
inline fun String.toGiphyData(): GiphyData =
    Json.decodeFromString(this)

@Suppress("NOTHING_TO_INLINE")
@Throws(AssertionError::class, EOFException::class)
inline fun GiphyData.toJson(): String =
    Json.encodeToString(this)

@Suppress("NOTHING_TO_INLINE")
inline fun GiphyData.retrieveImageUrlAndMessageMedia(): Pair<String, MessageMedia?>? {
    return if (url.isNotEmpty()) {
        Pair(url.replace("giphy.gif", "200w.gif"), null)
    } else {
        null
    }
}
// {"text": null,"url":"https://media3.giphy.com/media/StuemRSudGjTuu5QmY/giphy.gif?cid=d7a0fde9f4pgso0ojwyp6utwh63iiu9biqicdby6kv210sz5&rid=giphy.gif", "id":"StuemRSudGjTuu5QmY","aspect_ratio":"1.3259668508287292"}
@Serializable
data class GiphyData(
    val id: String,
    val url: String,
    val aspect_ratio: Double,
    val text: String?,
) {
    companion object {
        const val MESSAGE_PREFIX = "giphy::"
    }
}
