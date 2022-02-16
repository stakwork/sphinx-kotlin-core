package chat.sphinx.wrapper.podcast

data class FeedSearchResultRow(
    val feedSearchResult: FeedSearchResult?,
    val isSectionHeader: Boolean,
    val isFollowingSection: Boolean,
    val isLastOnSection: Boolean
)