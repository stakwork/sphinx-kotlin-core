package chat.sphinx.wrapper.feed

import chat.sphinx.utils.platform.File

interface DownloadableFeedItem {
    val id: FeedId
    val enclosureLength: FeedEnclosureLength?
    val enclosureUrl: FeedUrl
    val enclosureType: FeedEnclosureType?
    val localFile: File?
}