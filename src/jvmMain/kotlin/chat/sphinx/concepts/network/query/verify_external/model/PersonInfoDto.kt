package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonInfoDto(
    val owner_pubkey: String,
    val owner_alias: String,
    val owner_contact_key: String,
    val owner_route_hint: String?,
    val img: String?,
    val description: String?,
    val price_to_meet: Long?
)