package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.lightning.Sat
import kotlinx.serialization.Serializable

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPodBoostOrNull(): FeedBoost? =
    try {
        this.toPodBoost()
    } catch (e: Exception) {
        null
    }

fun String.toPodBoost(): FeedBoost =
    moshi.adapter(PodBoostMoshi::class.java)
        .fromJson(this)
        ?.let {
            FeedBoost(
                FeedId(it.feedID),
                FeedId(it.itemID),
                it.ts,
                Sat(it.amount)
            )
        }
        ?: throw IllegalArgumentException("Provided Json was invalid")

@Throws(AssertionError::class)
fun FeedBoost.toJson(): String =
    moshi.adapter(PodBoostMoshi::class.java)
        .toJson(
            PodBoostMoshi(
                feedId.value,
                itemId.value,
                timeSeconds,
                amount.value
            )
        )

data class FeedBoost(
    val feedId: FeedId,
    val itemId: FeedId,
    val timeSeconds: Int,
    val amount: Sat
) {
    companion object {
        const val MESSAGE_PREFIX = "boost::"
    }
}

// "{\"feedID\":\"226249\",\"itemID\":\"1997782557\",\"ts\":1396,\"amount\":100}"
@Serializable
internal data class PodBoostMoshi(
    val feedID: String,
    val itemID: String,
    val ts: Int,
    val amount: Long
)
