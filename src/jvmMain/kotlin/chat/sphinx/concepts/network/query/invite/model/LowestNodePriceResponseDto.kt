package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable

@Serializable
data class LowestNodePriceResponseDto(
    val price: Double
)