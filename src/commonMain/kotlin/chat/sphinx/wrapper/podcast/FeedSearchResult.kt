package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.feed.Feed

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.toDateTime
import chat.sphinx.wrapper.toPhotoUrl

fun FeedSearchResult.toFeed(): Feed? {
    return Feed(
        id = FeedId(id),
        feedType = feedType.toInt().toFeedType(),
        title = title.toFeedTitle() ?: FeedTitle("Unknown Feed"),
        description = description?.toFeedDescription(),
        feedUrl = url.toFeedUrl() ?: return null,
        author = author?.toFeedAuthor(),
        generator = generator?.toFeedGenerator(),
        imageUrl = imageUrl?.toPhotoUrl(),
        ownerUrl = ownerUrl?.toFeedUrl(),
        link = link?.toFeedUrl(),
        datePublished = datePublished?.toDateTime(),
        dateUpdated = dateUpdated?.toDateTime(),
        contentType = contentType?.toFeedContentType(),
        language = language?.toFeedLanguage(),
        itemsCount = FeedItemsCount(0),
        currentItemId = null,
        chatId = chatId?.let { ChatId(it) } ?: ChatId(ChatId.NULL_CHAT_ID.toLong()),
        subscribed = Subscribed.False
    )
}

data class FeedSearchResult(
    val id: String,
    val feedType: Long,
    val title: String,
    val url: String,
    val description: String?,
    val author: String?,
    val generator: String?,
    val imageUrl: String?,
    val ownerUrl: String?,
    val link: String?,
    val datePublished: Long?,
    val dateUpdated: Long?,
    val contentType: String?,
    val language: String?,
    val chatId: Long?
)