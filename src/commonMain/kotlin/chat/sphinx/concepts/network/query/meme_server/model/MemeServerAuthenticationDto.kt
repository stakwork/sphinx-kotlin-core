package chat.sphinx.concepts.network.query.meme_server.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemeServerAuthenticationDto(
    val id: String,
    val challenge: String,
)
