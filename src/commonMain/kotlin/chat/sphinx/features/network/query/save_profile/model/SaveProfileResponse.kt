package chat.sphinx.features.network.query.save_profile.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable


@Serializable
data class SaveProfileResponse(
    override val success: Boolean = true,
    override val response: Any?,
    override val error: String?
): RelayResponse<Any>()