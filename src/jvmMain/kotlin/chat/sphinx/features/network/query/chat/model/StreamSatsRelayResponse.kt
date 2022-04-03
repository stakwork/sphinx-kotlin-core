package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamSatsRelayResponse(
    override val success: Boolean,
    override val response: @Polymorphic Any? = null,
    override val error: String? = null
) : RelayResponse<Any>()