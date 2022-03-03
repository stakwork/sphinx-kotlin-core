package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.feed.FeedId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("NOTHING_TO_INLINE")
inline fun String.toPodcastClipOrNull(): PodcastClip? =
    try {
        this.toPodcastClip()
    } catch (e: Exception) {
        null
    }

fun String.toPodcastClip(): PodcastClip =
    Json.decodeFromString<PodcastClipMoshi>(this).let {
        PodcastClip(
            it.text,
            it.title,
            LightningNodePubKey(it.pubkey),
            it.url,
            FeedId(it.feedID),
            FeedId(it.itemID),
            it.ts
        )
    }

@Throws(AssertionError::class)
fun PodcastClip.toJson(): String =
    Json.encodeToString(
        PodcastClipMoshi(
            text ?: "",
            title,
            pubkey.value,
            url,
            feedID.value,
            itemID.value,
            ts
        )
    )

data class PodcastClip(
    val text: String?,
    val title: String,
    val pubkey: LightningNodePubKey,
    val url: String,
    val feedID: FeedId,
    val itemID: FeedId,
    val ts: Int,
) {

    companion object {
        const val MESSAGE_PREFIX = "clip::"
    }
}

@Serializable
internal data class PodcastClipMoshi(
    val text: String,
    val title: String,
    val pubkey: String,
    val url: String,
    val feedID: String,
    val itemID: String,
    val ts: Int,
)
