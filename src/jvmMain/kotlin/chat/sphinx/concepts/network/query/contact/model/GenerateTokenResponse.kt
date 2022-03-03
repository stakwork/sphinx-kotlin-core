package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTokenResponse(
    val id: Long
)
