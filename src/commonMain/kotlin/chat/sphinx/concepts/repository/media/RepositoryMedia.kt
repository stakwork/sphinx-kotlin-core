package chat.sphinx.concepts.repository.media

import chat.sphinx.wrapper.chat.ChatMetaData
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.feed.DownloadableFeedItem
import chat.sphinx.wrapper.feed.FeedDestination
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.message.Message
import chat.sphinx.wrapper.message.MessageUUID
import okio.Path

interface RepositoryMedia {

    fun updateChatMetaData(
        chatId: ChatId,
        podcastId: FeedId?,
        metaData: ChatMetaData,
        shouldSync: Boolean = true
    )

    suspend fun updateChatContentSeenAt(chatId: ChatId)

    fun downloadMediaIfApplicable(
        message: Message,
        sent: Boolean,
    )

    fun downloadMediaIfApplicable(
        feedItem: DownloadableFeedItem,
        downloadCompleteCallback: (downloadedFilePath: Path) -> Unit
    )

    fun inProgressDownloadIds(): List<FeedId>

    suspend fun deleteDownloadedMediaIfApplicable(
        feedItem: DownloadableFeedItem
    ): Boolean

    fun streamFeedPayments(
        chatId: ChatId,
        metaData: ChatMetaData,
        podcastId: String,
        episodeId: String,
        destinations: List<FeedDestination>,
        updateMetaData: Boolean = true,
        clipUUID: MessageUUID? = null
    )
}
