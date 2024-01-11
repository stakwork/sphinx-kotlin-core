package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonDataDto(
    val publicKey: String,
    val alias: String,
    val photoUrl: String?
)
