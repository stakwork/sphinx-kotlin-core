package chat.sphinx.wrapper.rsa

import io.ktor.util.*
import kotlin.jvm.JvmInline

@OptIn(InternalAPI::class)
@Suppress("NOTHING_TO_INLINE")
inline fun RsaSignatureString.toRsaSignature(): RsaSignature? =
    value.decodeBase64Bytes()?.let { RsaSignature(it) }

@JvmInline
value class RsaSignatureString(val value: String)

@OptIn(InternalAPI::class)
@Suppress("NOTHING_TO_INLINE")
inline fun RsaSignature.toRsaSignatureString(): RsaSignatureString =
    RsaSignatureString(value.encodeBase64())

@JvmInline
value class RsaSignature(val value: ByteArray) {
    @OptIn(InternalAPI::class)
    override fun toString(): String {
        return "RsaSignature(value=${value.encodeBase64()})"
    }
}

class RsaSignedString(
    val text: String,
    val signature: RsaSignature,
) {
    override fun toString(): String {
        return "RsaSignedString(text=$text, signature=$signature)"
    }
}
