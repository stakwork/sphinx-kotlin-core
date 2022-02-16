package chat.sphinx.concepts.network.query.chat.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostStreamSatsDestinationDto(
    val address: String,
    val type: String,
    val split: Double,
)