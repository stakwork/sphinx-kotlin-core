package chat.sphinx.concepts.network.query.lightning.model.invoice

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HashDto(
    val type: String,
    val data: List<Int>,
)
