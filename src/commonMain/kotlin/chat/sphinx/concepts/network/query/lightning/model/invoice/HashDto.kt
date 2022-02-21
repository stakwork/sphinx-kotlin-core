package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class HashDto(
    val type: String,
    val data: List<Int>,
)
