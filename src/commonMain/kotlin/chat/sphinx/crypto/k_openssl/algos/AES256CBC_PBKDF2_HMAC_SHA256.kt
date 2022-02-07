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
package chat.sphinx.crypto.k_openssl.algos

import chat.sphinx.crypto.common.annotations.RawPasswordAccess
import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import chat.sphinx.crypto.common.clazzes.*
import chat.sphinx.crypto.common.extensions.isValidUTF8
import chat.sphinx.crypto.common.extensions.toByteArray
import chat.sphinx.crypto.k_openssl.KOpenSSL
import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.SecureRandom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Methods for encrypting/decrypting data in a manner compliant with OpenSSL such that
 * they can be used interchangeably.
 *
 * OpenSSL Encryption from command line:
 *
 *   echo "Hello World!" | openssl aes-256-cbc -e -a -p -salt -pbkdf2 -iter 25000 -k password
 *
 * Terminal output (-p shows the following):
 *   salt=F71F01EC4171ACDF
 *   key=AF9344D72520323D210C440BA015526DABA0D22AD6247DFACF7D3F5B0F724A23
 *   iv =1125D64C744EDE615CE3B8AD55C1581C
 *   U2FsdGVkX1/3HwHsQXGs37PixswoJihPWATaxph4OVQ=
 *
 * OpenSSL Decryption from command line:
 *
 *   echo "U2FsdGVkX1/3HwHsQXGs37PixswoJihPWATaxph4OVQ=" | openssl aes-256-cbc -d -a -p -salt -pbkdf2 -iter 25000 -k password
 * */
@Suppress("ClassName", "SpellCheckingInspection")
class AES256CBC_PBKDF2_HMAC_SHA256: KOpenSSL() {

    /**
     * Decrypts an [EncryptedString] value.
     *
     * @return [UnencryptedString]
     * */
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun decrypt(
        password: Password,
        hashIterations: HashIterations,
        encryptedString: EncryptedString,
        dispatcher: CoroutineDispatcher
    ): UnencryptedString =
        decrypt(
            hashIterations,
            password,
            encryptedString,
            dispatcher
        ).let { unencryptedByteArray ->
            @OptIn(UnencryptedDataAccess::class)
            unencryptedByteArray.toUnencryptedString()
                .also {
                    unencryptedByteArray.clear()
                }
        }

    /**
     * Decrypts an [EncryptedString] value.
     *
     * @return [UnencryptedByteArray]
     * */
    @OptIn(RawPasswordAccess::class)
    override suspend fun decrypt(
        hashIterations: HashIterations,
        password: Password,
        encryptedString: EncryptedString,
        dispatcher: CoroutineDispatcher
    ): UnencryptedByteArray =
        withContext(dispatcher) {
            val encryptedBytes = decodeEncryptedString(encryptedString)

            // Salt is bytes 8 - 15
            val salt = encryptedBytes.copyOfRange(8, 16)

            // Cipher Text is bytes 16 - end of the encrypted bytes
            val cipherText = encryptedBytes.copyOfRange(16, encryptedBytes.size)

            // Decrypt the Cipher Text and manually remove padding after
            val decrypted = AES.decryptAesCbc(
                cipherText,
                password.value.toByteArray(),
                salt,
                Padding.NoPadding
            )

            if (!decrypted.isValidUTF8) {
                decrypted.fill('*'.code.toByte())
                throw CharacterCodingException()
            }

            // Last byte of the decrypted text is the number of padding bytes needed to remove
            UnencryptedByteArray(
                decrypted.copyOfRange(0, decrypted.size - decrypted.last().toInt())
            ).also {
                decrypted.fill('*'.code.toByte())
            }
        }

    /**
     * Encrypts an [UnencryptedString] value.
     *
     * @return [EncryptedString]
     * */
    @OptIn(UnencryptedDataAccess::class)
    override suspend fun encrypt(
        password: Password,
        hashIterations: HashIterations,
        unencryptedString: UnencryptedString,
        dispatcher: CoroutineDispatcher
    ): EncryptedString =
        unencryptedString.toUnencryptedByteArray().let { unencryptedByteArray ->
            encrypt(
                password,
                hashIterations,
                unencryptedByteArray,
                dispatcher
            ).also {
                unencryptedByteArray.clear()
            }
        }

    /**
     * Encrypts an [UnencryptedByteArray] value.
     *
     * @return [EncryptedString]
     * */
    @OptIn(UnencryptedDataAccess::class, RawPasswordAccess::class)
    override suspend fun encrypt(
        password: Password,
        hashIterations: HashIterations,
        unencryptedByteArray: UnencryptedByteArray,
        dispatcher: CoroutineDispatcher
    ): EncryptedString =
        withContext(dispatcher) {
            val salt = SecureRandom.nextBytes(8)

            val cipherText = AES.encryptAesCbc(
                unencryptedByteArray.value,
                password.value.toByteArray(),
                salt,
                Padding.PKCS7Padding
            )

            EncryptedString(encodeCipherText(salt, cipherText))
        }
}
