package chat.sphinx.features.network.query.version.model

import chat.sphinx.concepts.network.query.version.model.AppVersionsDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetAppVersionsRelayResponse(
    override val success: Boolean,
    override val response: AppVersionsDto? = null,
    override val error: String? = null
): RelayResponse<AppVersionsDto>()