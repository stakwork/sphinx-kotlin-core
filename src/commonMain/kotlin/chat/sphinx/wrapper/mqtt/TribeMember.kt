package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TribeMember(
    val pubkey: String? = null,
    val alias: String? = null,
    val photo_url: String? = null,
    val person: String? = null,
    val route_hint: String? = null,
    val contact_key: String? = null
)

@Serializable
data class TribeMembersResponse(
    val confirmed: List<TribeMember>? = null,
    val pending: List<TribeMember>? = null
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
