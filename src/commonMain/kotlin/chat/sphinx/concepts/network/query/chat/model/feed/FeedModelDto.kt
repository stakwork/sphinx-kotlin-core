package chat.sphinx.concepts.network.query.chat.model.feed

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedModelDto(
    val type: String,
    val suggested: Double,
)