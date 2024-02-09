package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

/**
 * Only non-null fields will be serialized to Json for the request body.
 * */
@Serializable
data class GenerateTokenAuthenticatedDto(
    val pubkey: String? = null,
    val password: String? = null
)
