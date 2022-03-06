package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

@Serializable
data class PostContactDto(
    val alias: String,
    val public_key: String,
    val status: Int,
    val route_hint: String? = null,
    val contact_key: String? = null,
    val photo_url: String? = null,
)
