package chat.sphinx.features.network.query.verify_external.model

import chat.sphinx.concepts.network.query.verify_external.model.VerifyExternalDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class VerifyExternalRelayResponse(
    override val success: Boolean,
    override val response: VerifyExternalDto?,
    override val error: String?
): RelayResponse<VerifyExternalDto>()