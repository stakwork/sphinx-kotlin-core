package chat.sphinx.concepts.network.query.invite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HubLowestNodePriceResponse(
    @Json(name = "object")
    val response: LowestNodePriceResponseDto?,

    val error: String?
)
