package chat.sphinx.features.repository.mappers.feed

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.ContentEpisodeStatusDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.podcast.ContentEpisodeStatus

internal class ContentEpisodeStatusDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<ContentEpisodeStatusDbo, ContentEpisodeStatus>(dispatchers) {
    override suspend fun mapFrom(value: ContentEpisodeStatusDbo): ContentEpisodeStatus {
        return ContentEpisodeStatus(
            value.feed_id,
            value.item_id,
            value.duration,
            value.current_time,
            value.played
        )
    }

    override suspend fun mapTo(value: ContentEpisodeStatus): ContentEpisodeStatusDbo {
        return ContentEpisodeStatusDbo(
            value.feedId,
            value.itemId,
            value.duration,
            value.currentTime,
            value.played
        )
    }
}