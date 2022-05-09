package chat.sphinx.wrapper.rsa

import io.matthewnelson.component.base64.encodeBase64
import kotlin.jvm.JvmInline

@JvmInline
value class RsaSignatureString(val value: String)

@Suppress("NOTHING_TO_INLINE")
inline fun RsaSignature.toRsaSignatureString(): RsaSignatureString =
    RsaSignatureString(value.encodeBase64())

@JvmInline
value class RsaSignature(val value: ByteArray) {
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
