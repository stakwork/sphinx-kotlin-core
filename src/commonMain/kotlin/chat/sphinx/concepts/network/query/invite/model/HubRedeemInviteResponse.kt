package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class HubRedeemInviteResponse(
    @JsonNames("object")
    val response: RedeemInviteResponseDto?,

    val error: String?
)
