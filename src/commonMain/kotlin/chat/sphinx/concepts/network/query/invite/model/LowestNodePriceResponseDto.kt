package chat.sphinx.concepts.network.query.invite.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LowestNodePriceResponseDto(
    val price: Double
)