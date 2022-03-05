package chat.sphinx.features.network.query.chat.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamSatsRelayResponse(
    override val success: Boolean,
    override val response: @Polymorphic Any?,
    override val error: String?
) : RelayResponse<Any>()