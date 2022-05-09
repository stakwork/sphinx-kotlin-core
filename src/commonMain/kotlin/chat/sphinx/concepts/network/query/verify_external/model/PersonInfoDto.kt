package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonInfoDto(
    val owner_pubkey: String,
    val owner_alias: String,
    val owner_contact_key: String,
    val owner_route_hint: String? = null,
    val img: String? = null,
    val description: String? = null,
    val price_to_meet: Long? = null
)