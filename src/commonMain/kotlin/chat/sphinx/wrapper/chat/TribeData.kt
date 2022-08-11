package chat.sphinx.wrapper.chat

import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.FeedUrl

data class TribeData(
    val host : ChatHost,
    val chatUUID: ChatUUID,
    val appUrl: AppUrl?,
    val feedUrl : FeedUrl?,
    val feedType: FeedType,
)