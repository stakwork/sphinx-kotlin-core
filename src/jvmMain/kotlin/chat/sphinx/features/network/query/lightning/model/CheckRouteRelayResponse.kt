package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.route.RouteSuccessProbabilityDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class CheckRouteRelayResponse(
    override val success: Boolean,
    override val response: RouteSuccessProbabilityDto? = null,
    override val error: String? = null
): RelayResponse<RouteSuccessProbabilityDto>()