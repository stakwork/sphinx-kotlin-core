package chat.sphinx.wrapper.contact

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.invite.InviteStatus
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.LightningRouteHint
import chat.sphinx.wrapper.lightning.Sat

data class NewContact(
    val contactAlias: ContactAlias?,
    val lightningNodePubKey: LightningNodePubKey?,
    val lightningRouteHint: LightningRouteHint?,
    val photoUrl: PhotoUrl?,
    val confirmed: Boolean,
    val inviteString: String?,
    val inviteCode: String?,
    val invitePrice: Sat?,
    val inviteStatus: InviteStatus?,
    val createdAt: DateTime?
)