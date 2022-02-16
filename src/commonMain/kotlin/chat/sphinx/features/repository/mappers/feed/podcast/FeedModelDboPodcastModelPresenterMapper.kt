package chat.sphinx.features.repository.mappers.feed.podcast

import chat.sphinx.concepts.coredb.FeedModelDbo
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.podcast.PodcastModel

internal class FeedModelDboPodcastModelPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedModelDbo, PodcastModel>(dispatchers) {
    override suspend fun mapFrom(value: FeedModelDbo): PodcastModel {
        return PodcastModel(
            type = value.type,
            suggested = value.suggested,
            podcastId = value.id
        )
    }

    override suspend fun mapTo(value: PodcastModel): FeedModelDbo {
        return FeedModelDbo(
            id = value.podcastId,
            type = value.type,
            suggested = value.suggested
        )
    }
}