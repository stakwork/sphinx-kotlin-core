package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.feed.FeedDestinationAddress
import chat.sphinx.wrapper.feed.FeedDestinationSplit
import chat.sphinx.wrapper.feed.FeedDestinationType
import chat.sphinx.wrapper.feed.FeedId


data class PodcastDestination(
    val split: FeedDestinationSplit,
    val address: FeedDestinationAddress,
    val type: FeedDestinationType,
    val podcastId: FeedId,
)