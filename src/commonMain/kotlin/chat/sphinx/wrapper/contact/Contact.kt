package chat.sphinx.wrapper.contact

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.InviteId
import chat.sphinx.wrapper.invite.InviteStatus
import chat.sphinx.wrapper.lightning.LightningNodeAlias
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.LightningRouteHint
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.rsa.RsaPublicKey

@Suppress("NOTHING_TO_INLINE")
inline fun Contact.isInviteContact(): Boolean =
    status.isPending() && inviteId != null

@Suppress("NOTHING_TO_INLINE")
inline fun Contact.isOnVirtualNode(): Boolean =
    routeHint != null && routeHint.value.isNotEmpty()

//inline val Contact.avatarUrl: URL?
//    get() {
//        return try {
//            if (photoUrl?.value != null) {
//                URL(photoUrl!!.value)
//            } else {
//                null
//            }
//        } catch (e: MalformedURLException) {
//            null
//        }
//    }

@Suppress("NOTHING_TO_INLINE")
inline fun Contact.getColorKey(): String {
    return "contact-${id.value}-color"
}

@Suppress("NOTHING_TO_INLINE")
inline fun Contact.isBlocked(): Boolean =
    blocked.isTrue()

@Suppress("NOTHING_TO_INLINE")
inline fun Contact.isEncrypted(): Boolean =
    rsaPublicKey?.value?.isNotEmpty() == true

data class Contact(
    val id: ContactId,
    val routeHint: LightningRouteHint?,
    val nodePubKey: LightningNodePubKey?,
    val nodeAlias: LightningNodeAlias?,
    val alias: ContactAlias?,
    val photoUrl: PhotoUrl?,
    val privatePhoto: PrivatePhoto,
    val isOwner: Owner,
    val status: ContactStatus,
    val rsaPublicKey: RsaPublicKey?,
    val deviceId: DeviceId?,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val fromGroup: ContactFromGroup,
    val notificationSound: NotificationSound?,
    val tipAmount: Sat?,
    val inviteId: InviteId?,
    val inviteStatus: InviteStatus?,
    val blocked: Blocked,
)
