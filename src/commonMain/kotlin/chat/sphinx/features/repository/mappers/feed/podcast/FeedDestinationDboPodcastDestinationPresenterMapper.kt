package chat.sphinx.features.repository.mappers.feed.podcast

import chat.sphinx.concepts.coredb.FeedDestinationDbo
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.podcast.PodcastDestination

internal class FeedDestinationDboPodcastDestinationPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedDestinationDbo, PodcastDestination>(dispatchers) {
    override suspend fun mapFrom(value: FeedDestinationDbo): PodcastDestination {
        return PodcastDestination(
            split = value.split,
            address = value.address,
            type = value.type,
            podcastId = value.feed_id
        )
    }

    override suspend fun mapTo(value: PodcastDestination): FeedDestinationDbo {
        return FeedDestinationDbo(
            address = value.address,
            split = value.split,
            type = value.type,
            feed_id = value.podcastId
        )
    }
}