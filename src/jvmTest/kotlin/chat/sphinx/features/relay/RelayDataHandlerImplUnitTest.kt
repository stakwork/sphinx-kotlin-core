package chat.sphinx.features.relay

import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.crypto.k_openssl.isSalted
import chat.sphinx.features.crypto_rsa.RSAAlgorithm
import chat.sphinx.features.crypto_rsa.RSAImpl
import chat.sphinx.test.features.authentication.core.AuthenticationCoreDefaultsTestHelper
import chat.sphinx.test.tor_manager.TestTorManager
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.*

class RelayDataHandlerImplUnitTest: AuthenticationCoreDefaultsTestHelper() {

    companion object {
        private const val RAW_URL = "https://some-endpoint.chat:3001"
        private const val RAW_JWT = "gsaAiFtGG/RfsaO"
    }

    private val testRSA: RSA by lazy {
        RSAImpl(RSAAlgorithm.RSA)
    }

    private val relayHandler: RelayDataHandler by lazy {
        RelayDataHandlerImpl(
            testStorage,
            testCoreManager,
            dispatchers,
            testHandler,
            TestTorManager(),
            testRSA
        )
    }


    @Test
    fun `persisted data is encrypted`() =
        testDispatcher.runBlockingTest {
            login()

            testStorage.getString(RelayDataHandlerImpl.RELAY_URL_KEY, null)?.let { encryptedUrl ->
                assertTrue(encryptedUrl.isSalted)
            } ?: fail("Failed to persist relay url to storage")

            assertTrue(relayHandler.persistAuthorizationToken(AuthorizationToken(RAW_JWT)))
            testStorage.getString(RelayDataHandlerImpl.RELAY_AUTHORIZATION_KEY, null)?.let { encryptedJwt ->
                assertTrue(encryptedJwt.isSalted)
            } ?: fail("Failed to persist relay jwt to storage")
        }

    @Test
    fun `clearing JavaWebToken updates storage properly`() =
        testDispatcher.runBlockingTest {
            login()

            relayHandler.persistAuthorizationToken(AuthorizationToken(RAW_JWT))
            testStorage.getString(RelayDataHandlerImpl.RELAY_AUTHORIZATION_KEY, null)?.let { encryptedJwt ->
                assertTrue(encryptedJwt.isSalted)
            } ?: fail("Failed to persist relay jwt to storage")

            relayHandler.persistAuthorizationToken(null)
            val notInStorage = "NOT_IN_STORAGE"
            testStorage.getString(RelayDataHandlerImpl.RELAY_AUTHORIZATION_KEY, notInStorage).let { jwt ->
                // default value is returned if persisted value is null
                if (jwt != notInStorage) {
                    fail("Java Web Token was not cleared from storage")
                }
            }
        }
}
