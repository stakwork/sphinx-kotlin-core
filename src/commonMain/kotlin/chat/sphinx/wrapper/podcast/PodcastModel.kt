package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.feed.FeedModelSuggested
import chat.sphinx.wrapper.feed.FeedModelType

data class PodcastModel(
    val type: FeedModelType,
    val suggested: FeedModelSuggested,
    val podcastId: FeedId,
)