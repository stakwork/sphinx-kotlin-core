package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

/**
 * Only non-null fields will be serialized to Json for the request body.
 * */
@Serializable
data class PutContactDto(
    val route_hint: String? = null,
    val public_key: String? = null,
    val node_alias: String? = null,
    val alias: String? = null,
    val photo_url: String? = null,
    val private_photo: Boolean? = null,
    val contact_key: String? = null,
    val device_id: String? = null,
    val notification_sound: String? = null,
    val tip_amount: Long? = null,
)
