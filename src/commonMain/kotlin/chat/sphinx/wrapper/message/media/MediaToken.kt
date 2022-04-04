package chat.sphinx.wrapper.message.media

import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.lightning.toSat
import chat.sphinx.wrapper.message.media.token.MediaHost
import chat.sphinx.wrapper.message.media.token.MediaMUID
import chat.sphinx.wrapper.message.media.token.toMediaHostOrNull
import chat.sphinx.wrapper.message.media.token.toMediaMUIDOrNull
import io.matthewnelson.component.base64.decodeBase64ToArray
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMediaToken(): MediaToken? =
    try {
        MediaToken(this)
    } catch (e: IllegalArgumentException) {
        null
    }

//Media
@Suppress("NOTHING_TO_INLINE")
inline fun MediaToken.getHostFromMediaToken(): MediaHost? =
    getMediaTokenElementWithIndex(0, true)?.toMediaHostOrNull()

@Suppress("NOTHING_TO_INLINE")
inline fun MediaToken.getMUIDFromMediaToken(): MediaMUID? =
    getMediaTokenElementWithIndex(1, false)?.toMediaMUIDOrNull()

@Suppress("NOTHING_TO_INLINE")
inline fun MediaToken.getPriceFromMediaToken(): Sat =
    getMediaAttributeWithName(MediaToken.AMT)
        ?.toLongOrNull()
        ?.toSat()
        ?: Sat(0)

@Suppress("NOTHING_TO_INLINE")
inline fun MediaToken.getMediaAttributeWithName(name: String): String? {
    getMediaTokenElementWithIndex(4, true)?.let { metaData ->
        metaData.split("&").let { metaDataItems ->
            for (item in metaDataItems) {
                if (item.contains("$name=")) {
                    return item.replace("$name=", "")
                }
            }
        }
    }
    return null
}

@Suppress("NOTHING_TO_INLINE")
inline fun MediaToken.getMediaTokenElementWithIndex(
    index: Int,
    base64Decoded: Boolean
): String? {
    value.split(".").let { splits ->
        if (splits.size > index) {
            val element = splits[index]

            return if (base64Decoded) {
                element.decodeBase64ToArray()?.toString(charset("UTF-8"))
            }  else {
                element
            }
        }
    }
    return null
}

@JvmInline
value class MediaToken(val value: String) {

    companion object {
        val PROVISIONAL_TOKEN = MediaToken("ProvisionalMediaToken")

        const val AMT = "amt"
    }

    init {
        require(value.isNotEmpty()) {
            "MediaToken cannot be empty"
        }
    }
}
