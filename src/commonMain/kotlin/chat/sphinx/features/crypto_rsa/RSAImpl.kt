package chat.sphinx.features.crypto_rsa

import chat.sphinx.concepts.crypto_rsa.KeySize
import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.crypto_rsa.SignatureAlgorithm
import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import chat.sphinx.crypto.common.clazzes.EncryptedString
import chat.sphinx.crypto.common.clazzes.UnencryptedByteArray
import chat.sphinx.crypto.common.clazzes.UnencryptedString
import chat.sphinx.platform.rsajava.RSA_PEM
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.rsa.*
import kotlinx.coroutines.CoroutineDispatcher


@Suppress("NOTHING_TO_INLINE")
inline fun RSA_PEM.clear(byte: Byte = '0'.code.toByte()) {
    Key_Modulus?.fill(byte)
    Key_Exponent?.fill(byte)
    Key_D?.fill(byte)
    Val_P?.fill(byte)
    Val_Q?.fill(byte)
    Val_DP?.fill(byte)
    Val_DQ?.fill(byte)
    Val_InverseQ?.fill(byte)
}

inline val RSA_PEM.blockSize: Int
    get() = Key_Modulus?.size ?: 0

inline val RSA_PEM.maxBytes: Int
    get() = blockSize - 11

@Suppress("SpellCheckingInspection")
expect open class RSAImpl: RSA {

    override suspend fun generateKeyPair(
        keySize: KeySize,
        dispatcher: CoroutineDispatcher?,
        pkcsType: PKCSType,
    ): Response<RSAKeyPair, ResponseError>

    override suspend fun decrypt(
        rsaPrivateKey: RsaPrivateKey,
        text: EncryptedString,
        dispatcher: CoroutineDispatcher,
    ): Response<UnencryptedByteArray, ResponseError>

    @OptIn(UnencryptedDataAccess::class)
    override suspend fun encrypt(
        rsaPublicKey: RsaPublicKey,
        text: UnencryptedString,
        formatOutput: Boolean,
        dispatcher: CoroutineDispatcher
    ): Response<EncryptedString, ResponseError>

    override suspend fun sign(
        rsaPrivateKey: RsaPrivateKey,
        text: String,
        algorithm: SignatureAlgorithm,
        dispatcher: CoroutineDispatcher
    ): Response<RsaSignedString, ResponseError>

    override suspend fun verifySignature(
        rsaPublicKey: RsaPublicKey,
        signedString: RsaSignedString,
        algorithm: SignatureAlgorithm,
        dispatcher: CoroutineDispatcher
    ): Response<Boolean, ResponseError>

}
