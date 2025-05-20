package chat.sphinx.features.repository.mappers.invite

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.InviteDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.invite.Invite

internal class InviteDboPresenterMapper(dispatchers: CoroutineDispatchers): ClassMapper<InviteDbo, Invite>(dispatchers) {

    override suspend fun mapFrom(value: InviteDbo): Invite {
        return Invite(
            id = value.id,
            inviteString = value.invite_string,
            paymentRequest = value.invoice,
            contactId = value.contact_id,
            status = value.status,
            price = value.price,
            createdAt = value.created_at,
            inviteCode = value.invite_code
        )
    }

    override suspend fun mapTo(value: Invite): InviteDbo {
        return InviteDbo(
            id = value.id,
            invite_string = value.inviteString,
            invoice = value.paymentRequest,
            contact_id = value.contactId,
            status = value.status,
            price = value.price,
            created_at = value.createdAt,
            invite_code = value.inviteCode
        )
    }
}
