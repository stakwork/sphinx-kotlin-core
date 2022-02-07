/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
package chat.sphinx.crypto.k_openssl

import chat.sphinx.crypto.common.clazzes.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.base64.decodeBase64ToArray
import okio.base64.encodeBase64
import kotlin.coroutines.cancellation.CancellationException

inline val CharSequence.isSalted: Boolean
    get() = try {
        lines().joinToString("")
            .decodeBase64ToArray()
            ?.copyOfRange(0, 8)
            ?.contentEquals(KOpenSSL.SALTED.encodeToByteArray())
            ?: false
    } catch (e: Exception) {
        false
    }

abstract class KOpenSSL {

    companion object {
        const val SALTED = "Salted__"
    }

    /**
     * Decrypts an [EncryptedString] value.
     *
     * @return [UnencryptedString]
     * */
    @Throws(
        CancellationException::class,
        CharacterCodingException::class,
        IllegalArgumentException::class,
        IllegalStateException::class, // bouncy castle DecoderException wrapper
        IndexOutOfBoundsException::class,
    )
    abstract suspend fun decrypt(
        password: Password,
        hashIterations: HashIterations,
        encryptedString: EncryptedString,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): UnencryptedString

    /**
     * Decrypts an [EncryptedString] value.
     *
     * @return [UnencryptedByteArray]
     * */
    @Throws(
        CancellationException::class,
        CharacterCodingException::class,
        IllegalArgumentException::class,
        IllegalStateException::class, // bouncy castle DecoderException wrapper
        IndexOutOfBoundsException::class
    )
    abstract suspend fun decrypt(
        hashIterations: HashIterations,
        password: Password,
        encryptedString: EncryptedString,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): UnencryptedByteArray

    /**
     * Encrypts an [UnencryptedString] value.
     *
     * @return [EncryptedString]
     * */
    @Throws(
        AssertionError::class,
        CancellationException::class,
        IllegalArgumentException::class,
        IllegalStateException::class, // bouncy castle EncoderException wrapper
        IndexOutOfBoundsException::class,
    )
    abstract suspend fun encrypt(
        password: Password,
        hashIterations: HashIterations,
        unencryptedString: UnencryptedString,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): EncryptedString

    /**
     * Encrypts an [UnencryptedByteArray] value.
     *
     * @return [EncryptedString]
     * */
    @Throws(
        AssertionError::class,
        CancellationException::class,
        IllegalArgumentException::class,
        IllegalStateException::class, // bouncy castle EncoderException wrapper
        IndexOutOfBoundsException::class,
    )
    abstract suspend fun encrypt(
        password: Password,
        hashIterations: HashIterations,
        unencryptedByteArray: UnencryptedByteArray,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): EncryptedString

    @Throws(IllegalStateException::class)
    protected open fun decodeEncryptedString(encryptedString: EncryptedString): ByteArray =
        encryptedString.value.lines().joinToString("")
            .decodeBase64ToArray()
            ?: throw IllegalStateException("Could not decode the provided string")

    @Throws(IllegalStateException::class)
    protected open fun encodeCipherText(salt: ByteArray, cipherText: ByteArray): String =
        (SALTED.encodeToByteArray() + salt + cipherText)
            .encodeBase64()
            .replace("(.{64})".toRegex(), "$1\n")

}
