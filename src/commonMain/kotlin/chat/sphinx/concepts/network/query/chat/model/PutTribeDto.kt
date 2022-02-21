package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class PutTribeDto(
    val name: String,
    val img: String = ""
)
