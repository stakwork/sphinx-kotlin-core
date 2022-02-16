package chat.sphinx.concepts.network.query.invite.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RedeemInviteResponseDto(
    val ip: String,
    val invite: RedeemInviteDto,
    val pubkey: String?
)
