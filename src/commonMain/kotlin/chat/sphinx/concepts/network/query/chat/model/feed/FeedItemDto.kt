package chat.sphinx.concepts.network.query.chat.model.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedItemDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val datePublished: Long? = null,
    val dateUpdated: Long? = null,
    val author: String? = null,
    val contentType: String? = null,
    val enclosureUrl: String,
    val enclosureType: String? = null,
    val enclosureLength: Long? = null,
    val duration: Long? = null,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val link: String? = null
)
