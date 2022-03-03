package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.GenerateTokenResponse
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
class GenerateTokenRelayResponse(
    override val success: Boolean,
    override val response: GenerateTokenResponse?,
    override val error: String?
) : RelayResponse<GenerateTokenResponse>()
