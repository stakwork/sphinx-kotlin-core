package chat.sphinx.features.repository.mappers.feed

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.FeedModelDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.feed.FeedModel

internal class FeedModelDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedModelDbo, FeedModel>(dispatchers) {
    override suspend fun mapFrom(value: FeedModelDbo): FeedModel {
        return FeedModel(
            value.id,
            value.type,
            value.suggested,
        )
    }

    override suspend fun mapTo(value: FeedModel): FeedModelDbo {
        return FeedModelDbo(
            value.id,
            value.type,
            value.suggested,
        )
    }
}