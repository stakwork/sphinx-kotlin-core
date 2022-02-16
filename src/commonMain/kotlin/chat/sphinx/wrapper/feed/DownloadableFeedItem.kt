package chat.sphinx.wrapper.feed

interface DownloadableFeedItem {
    val id: FeedId
    val enclosureLength: FeedEnclosureLength?
    val enclosureUrl: FeedUrl
    val enclosureType: FeedEnclosureType?
    val localFile: File?
}