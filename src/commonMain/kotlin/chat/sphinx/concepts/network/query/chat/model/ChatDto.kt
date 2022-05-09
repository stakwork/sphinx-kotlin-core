package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

@Serializable
data class ChatDto(
    val id: Long,
    val uuid: String,
    val name: String? = null,
    val photo_url: String? = null,
    val type: Int,
    val status: Int? = null,
    val contact_ids: List<Long>,
    val is_muted: Int? = null,
    val created_at: String,
    val updated_at: String,
    val deleted: Int? = null,
    val group_key: String? = null,
    val host: String? = null,
    val price_to_join: Long? = null,
    val price_per_message: Long? = null,
    val escrow_amount: Long? = null,
    val escrow_millis: Long? = null,
    val unlisted: Int? = null,
    val private: Int? = null,
    @JsonNames("owner_pubkey")
    val owner_pub_key: String? = null,
    val seen: Int? = null,
    val app_url: String? = null,
    val feed_url: String? = null,
    val meta: String? = null,
    val my_photo_url: String? = null,
    val my_alias: String? = null,
    val pending_contact_ids: List<Long>? = null
) {
    @Transient
    val isMutedActual: Boolean =
        is_muted?.let {
            it == 1
        } ?: false

    @Transient
    val deletedActual: Boolean =
        deleted?.let {
            it == 1
        } ?: false

    @Transient
    val unlistedActual: Boolean =
        unlisted?.let {
            it == 1
        } ?: false

    @Transient
    val privateActual: Boolean =
        private?.let {
            it == 1
        } ?: false

    @Transient
    val seenActual: Boolean =
        seen?.let {
            it == 1
        } ?: false
}
