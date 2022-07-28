package chat.sphinx.concepts.network.query.relay_keys.model

import kotlinx.serialization.Serializable

@Serializable
data class RelayTransportKeyDto(
    val transport_key: String,
)