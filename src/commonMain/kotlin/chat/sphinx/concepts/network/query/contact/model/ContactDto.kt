package chat.sphinx.concepts.network.query.contact.model

import chat.sphinx.concepts.network.query.invite.model.InviteDto
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
    val private_photo: Int? = null,
    val is_owner: Int? = null,
    val deleted: Int? = null,
    val auth_token: String? = null,
    val status: Int? = null,
    val contact_key: String? = null,
    val device_id: String? = null,
    val created_at: String,
    val updated_at: String,
    val from_group: Int? = null,
    val notification_sound: String? = null,
    val tip_amount: Long? = null,
    val invite: InviteDto? = null,
    val pending: Int? = null,
    val blocked: Int? = null,
) {
    @Transient
    val privatePhotoActual: Boolean =
        private_photo?.let {
            it == 1
        } ?: false

    @Transient
    val isOwnerActual: Boolean =
        is_owner?.let {
            it == 1
        } ?: false

    @Transient
    val deletedActual: Boolean =
        deleted?.let {
            it == 1
        } ?: false

    @Transient
    val blockedActual: Boolean =
        blocked?.let {
            it == 1
        } ?: false

    @Transient
    val fromGroupActual: Boolean =
        from_group?.let {
            it == 1
        } ?: false

    @Transient
    val pendingActual: Boolean =
        pending?.let {
            it == 1
        } ?: false
}
