package chat.sphinx.features.network.query.verify_external.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizeExternalResponse(
    override val success: Boolean = true,
    override val response: Any?,
    override val error: String?
): RelayResponse<Any>()