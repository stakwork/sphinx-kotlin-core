package chat.sphinx.concepts.network.query.feed_search.model

import chat.sphinx.wrapper.podcast.FeedSearchResult
import kotlinx.serialization.Serializable

@Serializable
data class FeedSearchResultDto(
    val id: String,
    val feedType: Long,
    val title: String,
    val url: String,
    val description: String,
    val author: String,
    val generator: String,
    val imageUrl: String,
    val ownerUrl: String,
    val link: String,
    val datePublished: Long,
    val dateUpdated: Long,
    val contentType: String,
    val language: String,
)

inline fun FeedSearchResultDto.toFeedSearchResult() : FeedSearchResult {
     return FeedSearchResult(
         id,
         feedType,
         title,
         url,
         description,
         author,
         generator,
         imageUrl,
         ownerUrl,
         link,
         datePublished,
         dateUpdated,
         contentType,
         language,
         null
     )
}