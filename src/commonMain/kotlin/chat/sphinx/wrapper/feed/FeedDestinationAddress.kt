package chat.sphinx.wrapper.feed

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toFeedDestinationAddress(): FeedDestinationAddress? =
    try {
        FeedDestinationAddress(this)
    } catch (e: IllegalArgumentException) {
        null
    }

inline val String.isValidFeedDestinationAddress: Boolean
    get() = isNotEmpty() && matches("^${FeedDestinationAddress.REGEX}\$".toRegex())

@JvmInline
value class FeedDestinationAddress(val value: String) {

    companion object {

        const val REGEX = "[A-F0-9a-f]{66}"

        fun fromByteArray(byteArray: ByteArray): FeedDestinationAddress {
            return FeedDestinationAddress(byteArray.decodeToString())
        }
    }

    init {
        require(value.isValidFeedDestinationAddress) {
            "Invalid Destination Address"
        }
    }
}