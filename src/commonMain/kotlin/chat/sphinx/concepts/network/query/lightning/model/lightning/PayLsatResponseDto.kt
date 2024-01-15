package chat.sphinx.concepts.network.query.lightning.model.lightning

import chat.sphinx.utils.SphinxJson
import kotlinx.serialization.Serializable

@Serializable
data class PayLsatResponseDto(
    val lsat: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPayLsatResponseDtoOrNull(): PayLsatResponseDto? =
    try {
        this.toPayLsatResponseDto()
    } catch (e: Exception) {
        null
    }

fun String.toPayLsatResponseDto(): PayLsatResponseDto =
    SphinxJson.decodeFromString<PayLsatResponseDto>(this).let {
        PayLsatResponseDto(
            it.lsat
        )
    }
