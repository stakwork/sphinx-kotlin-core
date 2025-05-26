package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.feed.FeedModelSuggested
import chat.sphinx.wrapper.feed.FeedModelType

data class PodcastModel(
    val type: FeedModelType,
    val suggested: FeedModelSuggested,
    val podcastId: FeedId,
) {
    companion object {
        private const val satsInBTC = 100_000_000
    }

    val suggestedSats: Long
        get() = (suggested.value * satsInBTC.toDouble()).toLong()
}