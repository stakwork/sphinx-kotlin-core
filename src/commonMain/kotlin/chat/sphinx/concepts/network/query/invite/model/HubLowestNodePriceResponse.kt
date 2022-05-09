package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class HubLowestNodePriceResponse(
    @JsonNames("object")
    val response: LowestNodePriceResponseDto? = null,

    val error: String? = null
)
