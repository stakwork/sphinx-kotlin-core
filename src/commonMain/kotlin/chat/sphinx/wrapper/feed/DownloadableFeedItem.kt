package chat.sphinx.wrapper.feed

import okio.Path

interface DownloadableFeedItem {
    val id: FeedId
    val enclosureLength: FeedEnclosureLength?
    val enclosureUrl: FeedUrl
    val enclosureType: FeedEnclosureType?
    val localFile: Path?
}