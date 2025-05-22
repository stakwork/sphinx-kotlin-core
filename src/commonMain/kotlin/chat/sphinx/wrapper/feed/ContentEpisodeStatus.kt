package chat.sphinx.wrapper.feed


data class ContentEpisodeStatus(
    val feedId: FeedId,
    val itemId: FeedId,
    val duration: FeedItemDuration,
    val currentTime: FeedItemDuration,
    val played: Boolean? = null
)