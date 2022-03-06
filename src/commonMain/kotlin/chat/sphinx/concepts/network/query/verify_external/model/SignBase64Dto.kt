package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable

@Serializable
data class SignBase64Dto(
    val sig: String
)