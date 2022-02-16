package chat.sphinx.wrapper.feed

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.dashboard.ChatId


inline val Feed.isPodcast: Boolean
    get() = feedType.isPodcast()

inline val Feed.isVideo: Boolean
    get() = feedType.isVideo()

inline val Feed.isNewsletter: Boolean
    get() = feedType.isNewsletter()

data class Feed(
    val id: FeedId,
    val feedType: FeedType,
    val title: FeedTitle,
    val description: FeedDescription?,
    val feedUrl: FeedUrl,
    val author: FeedAuthor?,
    val generator: FeedGenerator?,
    val imageUrl: PhotoUrl?,
    val ownerUrl: FeedUrl?,
    val link: FeedUrl?,
    val datePublished: DateTime?,
    val dateUpdated: DateTime?,
    val contentType: FeedContentType?,
    val language: FeedLanguage?,
    val itemsCount: FeedItemsCount,
    val currentItemId: FeedId?,
    val chatId: ChatId,
    val subscribed: Subscribed,
) {

    companion object {
        const val TRIBES_DEFAULT_SERVER_URL = "tribes.sphinx.chat"
    }

    var items: List<FeedItem> = listOf()

    var destinations: List<FeedDestination> = listOf()

    var model: FeedModel? = null

    var chat: Chat? = null

    var lastItem: FeedItem? = null
        get() {
            for (item in items) {
                if (item.id.value == currentItemId?.value) {
                    return item
                }
            }
            if (items.count() > 0) {
                return items.first()
            }
            return null
        }

    var imageUrlToShow: PhotoUrl? = null
        get() {
            imageUrl?.let {
                return it
            }
            chat?.photoUrl?.let {
                return it
            }
            return null
        }

    var titleToShow: String = ""
        get() = title.value.trim()

    var descriptionToShow: String = ""
        get() {
            return (description?.value ?: "").htmlToPlainText().trim()
        }

    val hasDestinations: Boolean
        get() = destinations.isNotEmpty()
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.htmlToPlainText(): String =
    this.replace(Regex("\\<[^>]*>"),"")