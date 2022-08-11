package chat.sphinx.concepts.network.query.invite.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class HubRedeemInviteResponse(
    @SerialName("object")
    @JsonNames("object")
    val response: RedeemInviteResponseDto? = null,

    val error: String? = null
)
