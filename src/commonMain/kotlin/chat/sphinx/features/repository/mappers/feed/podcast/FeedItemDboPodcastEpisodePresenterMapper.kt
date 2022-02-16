package chat.sphinx.features.repository.mappers.feed.podcast

import chat.sphinx.concepts.coredb.FeedItemDbo
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.podcast.PodcastEpisode

internal class FeedItemDboPodcastEpisodePresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedItemDbo, PodcastEpisode>(dispatchers) {
    override suspend fun mapFrom(value: FeedItemDbo): PodcastEpisode {
        return PodcastEpisode(
            id = value.id,
            title = value.title,
            description = value.description,
            image = value.image_url,
            link = value.link,
            enclosureUrl = value.enclosure_url,
            enclosureLength = value.enclosure_length,
            enclosureType =  value.enclosure_type,
            podcastId = value.feed_id,
            localFile = value.local_file
        )
    }

    override suspend fun mapTo(value: PodcastEpisode): FeedItemDbo {
        return FeedItemDbo(
            id = value.id,
            title = value.title,
            description = value.description,
            date_published = null,
            date_updated = null,
            author = null,
            content_type = null,
            enclosure_length = value.enclosureLength,
            enclosure_url = value.enclosureUrl,
            enclosure_type = value.enclosureType,
            duration = null,
            image_url = value.image,
            thumbnail_url = null,
            link = value.link,
            feed_id = value.podcastId,
            local_file = value.localFile
        )
    }
}