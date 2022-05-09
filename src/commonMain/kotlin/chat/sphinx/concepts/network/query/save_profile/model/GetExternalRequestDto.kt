package chat.sphinx.concepts.network.query.save_profile.model

import kotlinx.serialization.Serializable

@Suppress("NOTHING_TO_INLINE")
inline fun GetExternalRequestDto.isProfilePath(): Boolean =
    path == "profile"

@Suppress("NOTHING_TO_INLINE")
inline fun GetExternalRequestDto.isSaveMethod(): Boolean =
    method == "POST"

@Suppress("NOTHING_TO_INLINE")
inline fun GetExternalRequestDto.isDeleteMethod(): Boolean =
    method == "DELETE"

@Suppress("NOTHING_TO_INLINE")
inline fun GetExternalRequestDto.isClaimOnLiquidPath(): Boolean =
    path == "claim_on_liquid"


@Serializable
data class GetExternalRequestDto(
    val key: String,
    val body: String,
    val path: String,
    val method: String,
)