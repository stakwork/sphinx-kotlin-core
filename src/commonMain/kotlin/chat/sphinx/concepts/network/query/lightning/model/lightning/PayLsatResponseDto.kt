package chat.sphinx.concepts.network.query.lightning.model.lightning

import kotlinx.serialization.Serializable

@Serializable
data class PayLsatResponseDto(
    val lsat: String
)
