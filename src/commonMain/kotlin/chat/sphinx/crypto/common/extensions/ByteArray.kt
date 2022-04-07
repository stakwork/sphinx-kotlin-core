package chat.sphinx.crypto.common.extensions

//expect fun byteArrayToHex(value: ByteArray): String

/**
 * Convert ByteArray to String
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toHex(): String = StringBuilder(size * 2).let { hex ->
    for (b in this) {
        hex.append(String.format("%02x", b, 0xFF))
    }
    hex.toString()
}
