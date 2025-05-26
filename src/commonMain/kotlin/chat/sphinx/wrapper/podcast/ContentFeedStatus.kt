package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.feed.FeedUrl
import chat.sphinx.wrapper.feed.Subscribed
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.feed.FeedPlayerSpeed

data class ContentFeedStatus(
    val feedId: FeedId,
    val feedUrl: FeedUrl,
    val subscriptionStatus: Subscribed,
    val chatId: ChatId?,
    val itemId: FeedId?,
    val satsPerMinute: Sat?,
    val playerSpeed: FeedPlayerSpeed?
) {

    val actualChatId: ChatId?
        get() = if (chatId?.value == ChatId.NULL_CHAT_ID.toLong()) null else chatId
}