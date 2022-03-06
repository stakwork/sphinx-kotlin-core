package chat.sphinx.concepts.network.query.lightning.model.route

import kotlinx.serialization.Serializable

inline val RouteSuccessProbabilityDto.isRouteAvailable: Boolean
    get() = success_prob > 0

@Serializable
data class RouteSuccessProbabilityDto(
    val success_prob: Double
)