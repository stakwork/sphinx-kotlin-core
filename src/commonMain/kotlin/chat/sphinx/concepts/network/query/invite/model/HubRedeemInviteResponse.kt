package chat.sphinx.concepts.network.query.invite.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HubRedeemInviteResponse(
    @Json(name = "object")
    val response: RedeemInviteResponseDto?,

    val error: String?
)
