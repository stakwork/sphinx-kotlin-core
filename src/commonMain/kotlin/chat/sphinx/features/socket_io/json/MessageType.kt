package chat.sphinx.features.socket_io.json

import kotlinx.io.errors.IOException
import kotlinx.serialization.Serializable

@Suppress("NOTHING_TO_INLINE")
@Throws(IOException::class)
internal inline fun Moshi.getMessageType(json: String): MessageType =
    adapter(MessageType::class.java)
        .fromJson(json)
        ?: throw JsonDataException("Failed to convert SocketIO Message.type Json")

@Serializable
internal data class MessageType(val type: String)
