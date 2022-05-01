package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

@Serializable
data class GithubPATDto(
    val encrypted_pat: String? = null,
)