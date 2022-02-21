package chat.sphinx.concepts.network.query.save_profile.model

import kotlinx.serialization.Serializable

@Suppress("NOTHING_TO_INLINE")
inline fun GetPeopleProfileDto.isProfilePath(): Boolean =
    path == "profile"

@Suppress("NOTHING_TO_INLINE")
inline fun GetPeopleProfileDto.isSaveMethod(): Boolean =
    method == "POST"

@Suppress("NOTHING_TO_INLINE")
inline fun GetPeopleProfileDto.isDeleteMethod(): Boolean =
    method == "DELETE"

@Serializable
data class GetPeopleProfileDto(
    val key: String,
    val body: String,
    val path: String,
    val method: String,
)