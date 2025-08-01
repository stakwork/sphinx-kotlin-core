package chat.sphinx.concepts.network.query.save_profile.model

@kotlinx.serialization.Serializable
data class GiphyData(
    val id: String,
    val url: String,
    val aspect_ratio: Double,
    val text: String?
) {
    companion object {
        const val MESSAGE_PREFIX = "giphy::"
    }
}

@kotlinx.serialization.Serializable
data class GiphyResponse(
    val data: List<GiphyItem>,
    val pagination: GiphyPagination
)

@kotlinx.serialization.Serializable
data class GiphyItem(
    val id: String,
    val images: GiphyImages
)

@kotlinx.serialization.Serializable
data class GiphyImages(
    val original: GiphyImageData,
    val fixed_width: GiphyImageData
)

@kotlinx.serialization.Serializable
data class GiphyImageData(
    val url: String,
    val width: String,
    val height: String
)

@kotlinx.serialization.Serializable
data class GiphyPagination(
    val total_count: Int,
    val count: Int,
    val offset: Int
)