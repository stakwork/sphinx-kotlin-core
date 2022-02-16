package chat.sphinx.wrapper.feed

data class FeedDestination(
    val address: FeedDestinationAddress,
    val split: FeedDestinationSplit,
    val type: FeedDestinationType,
    val feedId: FeedId
)