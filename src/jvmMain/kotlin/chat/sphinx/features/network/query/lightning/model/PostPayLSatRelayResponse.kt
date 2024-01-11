package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.lightning.PayLsatResponseDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PostPayLSatRelayResponse(
    override val success: Boolean,
    override val response: PayLsatResponseDto? = null,
    override val error: String? = null
): RelayResponse<PayLsatResponseDto>()
