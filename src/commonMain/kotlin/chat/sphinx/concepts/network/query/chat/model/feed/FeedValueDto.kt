package chat.sphinx.concepts.network.query.chat.model.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedValueDto(
    val model: FeedModelDto,
    val destinations: List<FeedDestinationDto>,
)