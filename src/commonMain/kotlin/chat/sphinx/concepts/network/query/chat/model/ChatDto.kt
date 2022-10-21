package chat.sphinx.concepts.network.query.chat.model

import chat.sphinx.serialization.SphinxBoolean
import chat.sphinx.wrapper_chat.isMuteChat
import chat.sphinx.wrapper_chat.isOnlyMentions
import chat.sphinx.wrapper_chat.toNotificationLevel
import kotlinx.serialization.SerialName
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
    val is_muted: SphinxBoolean? = null,
    val created_at: String,
    val updated_at: String,
    val deleted: SphinxBoolean? = null,
    val group_key: String? = null,
    val host: String? = null,
    val price_to_join: Long? = null,
    val price_per_message: Long? = null,
    val escrow_amount: Long? = null,
    val escrow_millis: Long? = null,
    val unlisted: SphinxBoolean? = null,
    val private: SphinxBoolean? = null,
    @SerialName("owner_pubkey")
    @JsonNames("owner_pubkey")
    val owner_pub_key: String? = null,
    val seen: SphinxBoolean? = null,
    val app_url: String? = null,
    val feed_url: String? = null,
    val meta: String? = null,
    val my_photo_url: String? = null,
    val my_alias: String? = null,
    val pending_contact_ids: List<Long>? = null,
    val notify: Int?,
) {
    fun isMutedActual(): Boolean {
        notify?.let {
            return it.toNotificationLevel().isMuteChat()
        }
        return is_muted?.value ?: false
    }

    fun isOnlyMentions(): Boolean {
        notify?.let {
            return it.toNotificationLevel().isOnlyMentions()
        }
        return false
    }

    @Transient
    val deletedActual: Boolean = deleted?.value ?: false

    @Transient
    val unlistedActual: Boolean = unlisted?.value ?: false

    @Transient
    val privateActual: Boolean = private?.value ?: false

    @Transient
    val seenActual: Boolean = seen?.value ?: false
}
