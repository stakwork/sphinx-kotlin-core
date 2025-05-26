package chat.sphinx.wrapper.feed

data class FeedModel(
    val id: FeedId,
    val type: FeedModelType,
    val suggested: FeedModelSuggested
){
    companion object {
        private const val satsInBTC = 100_000_000
    }

    val suggestedSats: Long
        get() = (suggested.value * satsInBTC.toDouble()).toLong()
}