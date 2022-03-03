package chat.sphinx.concepts.network.query.meme_server.model

import kotlinx.serialization.Serializable

@Serializable
data class MemeServerAuthenticationDto(
    val id: String,
    val challenge: String,
)
