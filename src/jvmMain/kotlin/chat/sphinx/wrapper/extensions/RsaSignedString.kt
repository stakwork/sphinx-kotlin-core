package chat.sphinx.wrapper.extensions

import chat.sphinx.wrapper.rsa.RsaSignature
import chat.sphinx.wrapper.rsa.RsaSignatureString
import io.matthewnelson.component.base64.decodeBase64ToArray

@Suppress("NOTHING_TO_INLINE")
inline fun RsaSignatureString.toRsaSignature(): RsaSignature? =
    value.decodeBase64ToArray()?.let { RsaSignature(it) }