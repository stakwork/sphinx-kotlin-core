package chat.sphinx.concepts.repository.feed

import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.podcast.FeedSearchResultRow
import chat.sphinx.wrapper.podcast.Podcast
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getPodcastByChatId(chatId: ChatId): Flow<Podcast?>
    fun getPodcastById(feedId: FeedId): Flow<Podcast?>

    fun searchFeedsBy(
        searchTerm: String,
        feedType: FeedType?,
    ): Flow<List<FeedSearchResultRow>>

    suspend fun updateFeedContent(
        chatId: ChatId,
        host: ChatHost,
        feedUrl: FeedUrl,
        searchResultDescription: FeedDescription? = null,
        searchResultImageUrl: PhotoUrl? = null,
        chatUUID: ChatUUID?,
        subscribed: Subscribed,
        currentEpisodeId: FeedId?
    )

    fun getFeedByChatId(chatId: ChatId): Flow<Feed?>
    fun getFeedById(feedId: FeedId): Flow<Feed?>
    fun getFeedItemById(feedItemId: FeedId): Flow<FeedItem?>

    suspend fun toggleFeedSubscribeState(feedId: FeedId, currentSubscribeState: Subscribed)
}