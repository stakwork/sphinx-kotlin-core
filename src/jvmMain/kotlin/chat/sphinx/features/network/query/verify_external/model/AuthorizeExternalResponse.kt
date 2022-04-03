package chat.sphinx.features.network.query.verify_external.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizeExternalResponse(
    override val success: Boolean = true,
    override val response: @Polymorphic Any? = null,
    override val error: String? = null
): RelayResponse<Any>()