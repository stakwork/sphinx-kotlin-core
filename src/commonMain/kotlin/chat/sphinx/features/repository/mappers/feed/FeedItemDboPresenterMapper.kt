package chat.sphinx.features.repository.mappers.feed

import chat.sphinx.concepts.coredb.FeedItemDbo
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.feed.FeedItem

internal class FeedItemDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedItemDbo, FeedItem>(dispatchers) {
    override suspend fun mapFrom(value: FeedItemDbo): FeedItem {
        return FeedItem(
            value.id,
            value.title,
            value.description,
            value.date_published,
            value.date_updated,
            value.author,
            value.content_type,
            value.enclosure_length,
            value.enclosure_url,
            value.enclosure_type,
            value.image_url,
            value.thumbnail_url,
            value.link,
            value.feed_id,
            value.duration,
            value.local_file
        )
    }

    override suspend fun mapTo(value: FeedItem): FeedItemDbo {
        return FeedItemDbo(
            value.id,
            value.title,
            value.description,
            value.datePublished,
            value.dateUpdated,
            value.author,
            value.contentType,
            value.enclosureLength,
            value.enclosureUrl,
            value.enclosureType,
            value.imageUrl,
            value.thumbnailUrl,
            value.link,
            value.feedId,
            value.duration,
            value.localFile
        )
    }
}