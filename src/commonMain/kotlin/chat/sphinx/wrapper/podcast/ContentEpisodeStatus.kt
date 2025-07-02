package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.feed.FeedItemDuration


data class ContentEpisodeStatus(
    val feedId: FeedId,
    val itemId: FeedId,
    val duration: FeedItemDuration,
    val currentTime: FeedItemDuration,
    val played: Boolean? = null,
    val lastPlayed: DateTime? = null
)