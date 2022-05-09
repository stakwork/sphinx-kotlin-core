package chat.sphinx.crypto.common.clazzes

import chat.sphinx.crypto.common.extensions.toHex
import java.security.MessageDigest

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toSha256Hash(): Sha256Hash {
    MessageDigest.getInstance("SHA-256").let { digest ->
        digest.reset()
        digest.update(this, 0, size)
        return Sha256Hash(digest.digest().toHex())
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.toSha256Hash(): Sha256Hash =
    encodeToByteArray().toSha256Hash()

@JvmInline
value class Sha256Hash(val value: String) {

    init {
        require(isValid(value)) {
            "$value is not a valid Sha256 hash"
        }
    }

    companion object {
        fun isValid(sha256Hash: String): Boolean =
            sha256Hash.matches("[a-f0-9]{64}".toRegex())

        fun isValid(sha256Hash: ByteArray): Boolean =
            isValid(sha256Hash.toHex())
    }
}