package chat.sphinx.wrapper.chat

import chat.sphinx.wrapper.ItemId
import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.lightning.Sat
import kotlinx.serialization.Serializable

@Suppress("NOTHING_TO_INLINE")
inline fun String.toChatMetaDataOrNull(): ChatMetaData? =
    try {
        this.toChatMetaData()
    } catch (e: Exception) {
        null
    }

@Throws(
    IllegalArgumentException::class
)
fun String.toChatMetaData(): ChatMetaData {
    val chatMetaData = try {
        moshi.adapter(ChatMetaDataLongIdMoshi::class.java)
            .fromJson(this)
            ?.let {
                ChatMetaData(
                    FeedId(it.itemID.toString()),
                    ItemId(it.itemID),
                    Sat(it.sats_per_minute),
                    it.ts,
                    it.speed
                )
            }
    } catch (e: Exception) {
        null
    }

    return chatMetaData ?: run {
        moshi.adapter(ChatMetaDataStringIdMoshi::class.java)
            .fromJson(this)
            ?.let {
                ChatMetaData(
                    FeedId(it.itemID),
                    ItemId(-1),
                    Sat(it.sats_per_minute),
                    it.ts,
                    it.speed
                )
            }
            ?: throw IllegalArgumentException("Provided Json was invalid")
    }
}

@Throws(AssertionError::class)
fun ChatMetaData.toJson(): String {
    return if (itemLongId.value >= 0) {
        moshi.adapter(ChatMetaDataLongIdMoshi::class.java)
            .toJson(
                ChatMetaDataLongIdMoshi(
                    itemLongId.value,
                    satsPerMinute.value,
                    timeSeconds,
                    speed
                )
            )
    } else {
        moshi.adapter(ChatMetaDataStringIdMoshi::class.java)
            .toJson(
                ChatMetaDataStringIdMoshi(
                    itemId.value,
                    satsPerMinute.value,
                    timeSeconds,
                    speed
                )
            )
    }
}


data class ChatMetaData(
    val itemId: FeedId,
    val itemLongId: ItemId,
    val satsPerMinute: Sat,
    val timeSeconds: Int,
    val speed: Double,
)

// ItemID as String
// "{\"itemID\":\"1922435539"\,\"sats_per_minute\":3,\"ts\":4, \"speed\":1.5}"
@Serializable
internal data class ChatMetaDataStringIdMoshi(
    val itemID: String,
    val sats_per_minute: Long,
    val ts: Int,
    val speed: Double,
)

// ItemID as Long to be compatible with old version
// "{\"itemID\":1922435539,\"sats_per_minute\":3,\"ts\":4, \"speed\":1.5}"
@Serializable
internal data class ChatMetaDataLongIdMoshi(
    val itemID: Long,
    val sats_per_minute: Long,
    val ts: Int,
    val speed: Double,
)
