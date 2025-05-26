package chat.sphinx.features.repository.mappers.feed

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.ContentFeedStatusDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.podcast.ContentFeedStatus


internal class ContentFeedStatusDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<ContentFeedStatusDbo, ContentFeedStatus>(dispatchers) {
    override suspend fun mapFrom(value: ContentFeedStatusDbo): ContentFeedStatus {
        return ContentFeedStatus(
            value.feed_id,
            value.feed_url,
            value.subscription_status,
            value.chat_id,
            value.item_id,
            value.sats_per_minute,
            value.player_speed
        )
    }

    override suspend fun mapTo(value: ContentFeedStatus): ContentFeedStatusDbo {
        return ContentFeedStatusDbo(
            value.feedId,
            value.feedUrl,
            value.subscriptionStatus,
            value.chatId,
            value.itemId,
            value.satsPerMinute,
            value.playerSpeed
        )
    }
}