package chat.sphinx.concepts.network.query.contact.model

import chat.sphinx.concepts.network.query.invite.model.InviteDto
import chat.sphinx.serialization.SphinxBoolean
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ContactDto(
    val id: Long,
    val route_hint: String? = null,
    val public_key: String? = null,
    val node_alias: String? = null,
    val alias: String? = null,
    val photo_url: String? = null,
    val private_photo: SphinxBoolean? = null,
    val is_owner: SphinxBoolean? = null,
    val deleted: SphinxBoolean? = null,
    val auth_token: String? = null,
    val status: Int? = null,
    val contact_key: String? = null,
    val device_id: String? = null,
    val created_at: String,
    val updated_at: String,
    val from_group: SphinxBoolean? = null,
    val notification_sound: String? = null,
    val tip_amount: Long? = null,
    val invite: InviteDto? = null,
    val pending: SphinxBoolean? = null,
    val blocked: SphinxBoolean? = null,
) {
    @Transient
    val privatePhotoActual: Boolean = private_photo?.value ?: false

    @Transient
    val isOwnerActual: Boolean = is_owner?.value ?: false

    @Transient
    val deletedActual: Boolean = deleted?.value ?: false

    @Transient
    val blockedActual: Boolean = blocked?.value ?: false

    @Transient
    val fromGroupActual: Boolean = from_group?.value ?: false

    @Transient
    val pendingActual: Boolean = pending?.value ?: false
}
