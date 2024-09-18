package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TribeMember(
    val pubkey: String?,
    val alias: String?,
    val photo_url: String?,
    val person: String?,
    val route_hint: String?,
    val contact_key: String?
)

@Serializable
data class TribeMembersResponse(
    val confirmed: List<TribeMember>?,
    val pending: List<TribeMember>?
) {
    companion object {
        fun String.toTribeMembersList(): TribeMembersResponse? {
            return try {
                Json.decodeFromString<TribeMembersResponse>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
