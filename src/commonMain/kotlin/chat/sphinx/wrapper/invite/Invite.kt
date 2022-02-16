package chat.sphinx.wrapper.invite


import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.InviteId
import chat.sphinx.wrapper.lightning.LightningPaymentRequest
import chat.sphinx.wrapper.lightning.Sat

data class Invite(
    val id: InviteId,
    val inviteString: InviteString,
    val paymentRequest: LightningPaymentRequest?,
    val contactId: ContactId,
    val status: InviteStatus,
    val price: Sat?,
    val createdAt: DateTime,
)
