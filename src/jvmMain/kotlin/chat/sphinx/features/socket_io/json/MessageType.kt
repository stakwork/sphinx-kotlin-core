package chat.sphinx.features.socket_io.json

import kotlinx.io.errors.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun String.getMessageType(): MessageType =
    Json.decodeFromString(this)

@Serializable
internal data class MessageType(val type: String)
