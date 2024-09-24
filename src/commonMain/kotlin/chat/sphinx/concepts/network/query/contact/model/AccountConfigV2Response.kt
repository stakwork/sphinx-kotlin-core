package chat.sphinx.concepts.network.query.contact.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountConfigV2Response(
    val tribe: String,
    val tribe_host: String,
    val router: String,
    val default_lsp: String
)