package chat.sphinx.features.repository.mappers.invite

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.InviteDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.invite.Invite

internal class InviteDboPresenterMapper(dispatchers: CoroutineDispatchers): ClassMapper<InviteDbo, Invite>(dispatchers) {
    override suspend fun mapFrom(value: InviteDbo): Invite {
        return Invite(
            value.id,
            value.invite_string,
            value.invoice,
            value.contact_id,
            value.status,
            value.price,
            value.created_at,
        )
    }

    override suspend fun mapTo(value: Invite): InviteDbo {
        return InviteDbo(
            value.id,
            value.inviteString,
            value.paymentRequest,
            value.contactId,
            value.status,
            value.price,
            value.createdAt,
        )
    }
}
