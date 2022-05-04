package chat.sphinx.features.repository.mappers.feed.podcast

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.FeedDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.feed.FeedItemsCount
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.toFeedId
import chat.sphinx.wrapper.podcast.Podcast


internal class FeedDboPodcastPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<FeedDbo, Podcast>(dispatchers) {
    override suspend fun mapFrom(value: FeedDbo): Podcast {
        val podcast =  Podcast(
            id = value.id,
            title = value.title,
            description = value.description,
            author = value.author,
            image = value.image_url,
            datePublished = value.date_published,
            chatId = value.chat_id,
            feedUrl = value.feed_url,
            subscribed = value.subscribed
        )
        podcast.episodeId = value.current_item_id?.value
        return podcast
    }

    override suspend fun mapTo(value: Podcast): FeedDbo {
        return FeedDbo(
            id = value.id,
            feed_type = FeedType.Podcast,
            title = value.title,
            description = value.description,
            feed_url = value.feedUrl,
            author = value.author,
            generator = null,
            image_url = value.image,
            owner_url = null,
            link = null,
            date_published = value.datePublished,
            date_updated = null,
            content_type = null,
            language = null,
            items_count = FeedItemsCount(value.episodes.count().toLong()),
            current_item_id = value.episodeId?.toFeedId(),
            chat_id = value.chatId,
            subscribed = value.subscribed
        )
    }
}