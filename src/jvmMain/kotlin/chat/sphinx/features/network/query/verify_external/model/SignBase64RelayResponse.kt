package chat.sphinx.features.network.query.verify_external.model

import chat.sphinx.concepts.network.query.verify_external.model.SignBase64Dto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class SignBase64RelayResponse(
    override val success: Boolean,
    override val response: SignBase64Dto? = null,
    override val error: String? = null
): RelayResponse<SignBase64Dto>()