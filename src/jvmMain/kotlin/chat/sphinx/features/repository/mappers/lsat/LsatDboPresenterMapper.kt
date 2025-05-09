package chat.sphinx.features.repository.mappers.lsat

import chat.sphinx.database.core.LsatDbo
import chat.sphinx.wrapper.lsat.Lsat
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.repository.mappers.ClassMapper


internal class LsatDboPresenterMapper(
    dispatchers: CoroutineDispatchers
): ClassMapper<LsatDbo, Lsat>(dispatchers) {

    override suspend fun mapFrom(value: LsatDbo): Lsat {
        return Lsat(
            id = value.id,
            macaroon = value.macaroon,
            paymentRequest = value.payment_request,
            issuer = value.issuer,
            metaData = value.meta_data,
            paths = value.paths,
            preimage = value.preimage,
            status = value.status,
            createdAt = value.created_at
        )
    }

    override suspend fun mapTo(value: Lsat): LsatDbo {
        return LsatDbo(
            id = value.id,
            macaroon = value.macaroon,
            payment_request = value.paymentRequest,
            issuer = value.issuer,
            meta_data = value.metaData,
            paths = value.paths,
            preimage = value.preimage,
            status = value.status,
            created_at = value.createdAt
        )
    }
}
