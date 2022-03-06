package chat.sphinx.concepts.network.query.contact.model

import chat.sphinx.concepts.network.query.invite.model.InviteDto
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ContactDto(
    val id: Long,
    val route_hint: String?,
    val public_key: String?,
    val node_alias: String?,
    val alias: String?,
    val photo_url: String?,
    val private_photo: @Polymorphic Any?,
    val is_owner: @Polymorphic Any?,
    val deleted: @Polymorphic Any?,
    val auth_token: String?,
    val status: Int?,
    val contact_key: String?,
    val device_id: String?,
    val created_at: String,
    val updated_at: String,
    val from_group: @Polymorphic Any?,
    val notification_sound: String?,
    val tip_amount: Long?,
    val invite: InviteDto?,
    val pending: @Polymorphic Any?,
    val blocked: @Polymorphic Any?,
) {
    @Transient
    val privatePhotoActual: Boolean =
        when (private_photo) {
            is Boolean -> {
                private_photo
            }
            is Double -> {
                private_photo.toInt() == 1
            }
            else -> {
                false
            }
        }

    @Transient
    val isOwnerActual: Boolean =
        when (is_owner) {
            is Boolean -> {
                is_owner
            }
            is Double -> {
                is_owner.toInt() == 1
            }
            else -> {
                false
            }
        }

    @Transient
    val deletedActual: Boolean =
        when (deleted) {
            is Boolean -> {
                deleted
            }
            is Double -> {
                deleted.toInt() == 1
            }
            else -> {
                false
            }
        }

    @Transient
    val blockedActual: Boolean =
        when (blocked) {
            is Boolean -> {
                blocked
            }
            is Double -> {
                blocked.toInt() == 1
            }
            is Long -> {
                blocked == 1
            }
            else -> {
                false
            }
        }

    @Transient
    val fromGroupActual: Boolean =
        when (from_group) {
            is Boolean -> {
                from_group
            }
            is Double -> {
                from_group.toInt() == 1
            }
            else -> {
                false
            }
        }

    @Transient
    val pendingActual: Boolean =
        when (pending) {
            is Boolean -> {
                pending
            }
            is Double -> {
                pending.toInt() == 1
            }
            else -> {
                false
            }
        }
}
