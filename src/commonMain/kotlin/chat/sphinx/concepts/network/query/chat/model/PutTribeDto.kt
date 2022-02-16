package chat.sphinx.concepts.network.query.chat.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PutTribeDto(
    val name: String,
    val img: String = ""
)
