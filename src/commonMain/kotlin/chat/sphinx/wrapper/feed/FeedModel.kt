package chat.sphinx.wrapper.feed

data class FeedModel(
    val id: FeedId,
    val type: FeedModelType,
    val suggested: FeedModelSuggested
)