package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class PutChatDto(
    val my_alias: String? = null,
    val my_photo_url: String? = null,
    val meta: String? = null,
)
