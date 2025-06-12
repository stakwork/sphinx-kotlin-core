package chat.sphinx.concepts.repository.feed

import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.MessageUUID
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

    fun updateContentFeedStatus(
        feedId: FeedId,
        feedUrl: FeedUrl,
        subscriptionStatus: Subscribed,
        chatId: ChatId?,
        itemId: FeedId?,
        satsPerMinute: Sat?,
        playerSpeed: FeedPlayerSpeed?,
        shouldSync: Boolean = false
    )

    fun updateContentEpisodeStatus(
        feedId: FeedId,
        itemId: FeedId,
        duration: FeedItemDuration,
        currentTime: FeedItemDuration,
        played: Boolean = false,
        shouldSync: Boolean = false
    )

    fun streamFeedPayments(
        chatId: ChatId,
        feedId: String,
        feedItemId: String,
        currentTime: Long,
        amount: Sat?,
        playerSpeed: FeedPlayerSpeed?,
        destinations: List<FeedDestination>,
        clipMessageUUID: MessageUUID? = null
    )

    fun updatePlayedMark(feedItemId: FeedId, played: Boolean)
    fun getPlayedMark(feedItemId: FeedId): Flow<Boolean?>


    suspend fun toggleFeedSubscribeState(feedId: FeedId, currentSubscribeState: Subscribed)
}