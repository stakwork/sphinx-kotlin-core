package chat.sphinx.feature_repository.model.tribe

import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.FeedUrl

data class TribeData(
    private val host : ChatHost,
    private val chatUUID: ChatUUID,
    private val feedUrl : FeedUrl,
    private val feedType: FeedType,
)