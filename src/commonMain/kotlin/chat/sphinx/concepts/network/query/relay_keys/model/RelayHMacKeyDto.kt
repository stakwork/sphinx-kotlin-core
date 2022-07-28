package chat.sphinx.concepts.network.query.relay_keys.model

import kotlinx.serialization.Serializable

@Serializable
data class RelayHMacKeyDto(
    val encrypted_key: String,
)