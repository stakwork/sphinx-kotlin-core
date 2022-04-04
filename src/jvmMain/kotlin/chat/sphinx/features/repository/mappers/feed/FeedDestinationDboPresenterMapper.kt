package chat.sphinx.features.repository.mappers.feed

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.FeedDestinationDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.feed.FeedDestination

internal class FeedDestinationDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedDestinationDbo, FeedDestination>(dispatchers) {
    override suspend fun mapFrom(value: FeedDestinationDbo): FeedDestination {
        return FeedDestination(
            value.address,
            value.split,
            value.type,
            value.feed_id
        )
    }

    override suspend fun mapTo(value: FeedDestination): FeedDestinationDbo {
        return FeedDestinationDbo(
            value.address,
            value.split,
            value.type,
            value.feedId
        )
    }
}