package chat.sphinx.concepts.network.query.contact.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateTokenResponse(
    val id: Long
)
