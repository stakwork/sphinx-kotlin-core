package chat.sphinx.features.authentication.core

import chat.sphinx.concepts.authentication.coordinator.AuthenticationRequest
import chat.sphinx.concepts.authentication.coordinator.AuthenticationResponse
import chat.sphinx.concepts.authentication.core.model.UserInput
import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.crypto.common.annotations.RawPasswordAccess
import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import chat.sphinx.crypto.common.clazzes.EncryptedString
import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.crypto.common.clazzes.UnencryptedString
import chat.sphinx.crypto.k_openssl.algos.AES256CBC_PBKDF2_HMAC_SHA256
import chat.sphinx.features.authentication.core.model.AuthenticateFlowResponse
import chat.sphinx.features.authentication.core.model.Credentials
import chat.sphinx.features.authentication.core.model.UserInputWriter
import chat.sphinx.test.features.authentication.core.AuthenticationCoreDefaultsTestHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.*

@OptIn(RawPasswordAccess::class, UnencryptedDataAccess::class)
class AuthenticationCoreManagerUnitTest: AuthenticationCoreDefaultsTestHelper() {

    private fun getValidUserInput(
        c: Char,
        repeat: Int = testInitializer.minimumUserInputLength
    ): UserInput {
        val input = testCoreManager.getNewUserInput()
        repeat(repeat) {
            input.addCharacter(c)
        }
        return input
    }

    @Test
    fun `isAnEncryptionKeySet returns correct boolean value if encryption key is in storage`() =
        testDispatcher.runBlockingTest {
            assertFalse(testCoreManager.isAnEncryptionKeySet())
            login()
            assertTrue(testCoreManager.isAnEncryptionKeySet())
        }

    @Test
    fun `empty public key is stored as EMPTY`() =
        testDispatcher.runBlockingTest {
            assertFalse(testCoreManager.isAnEncryptionKeySet())

            // test handler generates empty public key values
            assertTrue(testHandler.generateEncryptionKey().publicKey.value.isEmpty())

            // set credentials/keys for first time
            login()

            val privateKey: Password = testCoordinator.submitAuthenticationRequest(
                AuthenticationRequest.GetEncryptionKey()
            ).first().let { response ->
                if (response is AuthenticationResponse.Success.Key) {
                    // Set EncryptionKey's public key is empty char array
                    assertTrue(response.encryptionKey.publicKey.value.isEmpty())

                    response.encryptionKey.privateKey
                } else {
                    throw AssertionError()
                }
            }

            val kOpenSSL = AES256CBC_PBKDF2_HMAC_SHA256()
            testStorage.storage[AuthenticationStorage.CREDENTIALS]?.let { credsString ->

                // credentials string has 3 concatenated string values
                credsString.split(Credentials.DELIMITER).let { splits ->
                    assertTrue(splits.size == 3)
                    val decrypted: UnencryptedString = kOpenSSL.decrypt(
                        privateKey,
                        testHandler.getTestStringEncryptHashIterations(privateKey),
                        EncryptedString(splits[1]),
                        dispatchers.default
                    )

                    // decrypted string value is actually "EMPTY"
                    assertEquals(Credentials.EMPTY, decrypted.value)
                }

                // This would throw an AuthenticationException if empty
                val publicKey: Password = Credentials.fromString(credsString)
                    .decryptPublicKey(
                        dispatchers,
                        privateKey,
                        testHandler,
                        kOpenSSL
                    )

                // decrypting the public key returns a Password containing an empty char array
                assertTrue(publicKey.value.isEmpty())

            } ?: fail("Storage was null")
        }

    @Test
    fun `min user input length not met returns error`() =
        testDispatcher.runBlockingTest {
            val input = getValidUserInput('a')
            input.dropLastCharacter()

            // Authenticate API
            val request = AuthenticationRequest.LogIn(privateKey = null)
            testCoreManager.authenticate(input, listOf(request)).first().let { flowResponse ->
                if (flowResponse !is AuthenticateFlowResponse.Error.Authenticate.InvalidPasswordEntrySize) {
                    fail()
                }
            }

            // SetPasswordFirstTime AIP
            val setPasswordResponse = AuthenticateFlowResponse
                .ConfirmInputToSetForFirstTime.instantiate(input as UserInputWriter)
            testCoreManager.setPasswordFirstTime(setPasswordResponse, input, listOf(request))
                .first().let { flowResponse ->
                    if (flowResponse !is AuthenticateFlowResponse.Error.SetPasswordFirstTime.InvalidNewPasswordEntrySize) {
                        fail()
                    }
                }

            // ResetPassword API
            val resetRequest = AuthenticationRequest.ResetPassword()
            val resetResponse = AuthenticateFlowResponse
                .PasswordConfirmedForReset.generate(input, resetRequest)!! // won't be on the list
            resetResponse.storeNewPasswordToBeSet(input)
            testCoreManager.resetPassword(resetResponse, input, listOf(resetRequest))
                .first().let { flowResponse ->
                    if (flowResponse !is AuthenticateFlowResponse.Error.ResetPassword.InvalidNewPasswordEntrySize) {
                        fail()
                    }
                }
        }
}