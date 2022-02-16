package chat.sphinx.test.network.query

import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.invite.NetworkQueryInvite
import chat.sphinx.concepts.network.query.lightning.NetworkQueryLightning
import chat.sphinx.concepts.network.query.message.NetworkQueryMessage
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.subscription.NetworkQuerySubscription
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.network.query.version.NetworkQueryVersion
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.network.tor.TorManager
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.features.network.client.NetworkClientImpl
import chat.sphinx.features.network.query.chat.NetworkQueryChatImpl
import chat.sphinx.features.network.query.contact.NetworkQueryContactImpl
import chat.sphinx.features.network.query.invite.NetworkQueryInviteImpl
import chat.sphinx.features.network.query.lightning.NetworkQueryLightningImpl
import chat.sphinx.features.network.query.message.NetworkQueryMessageImpl
import chat.sphinx.features.network.query.save_profile.NetworkQuerySaveProfileImpl
import chat.sphinx.features.network.query.verify_external.NetworkQueryAuthorizeExternalImpl
import chat.sphinx.features.network.relay_call.NetworkRelayCallImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
import chat.sphinx.logger.LogType
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.test.features.authentication.core.AuthenticationCoreDefaultsTestHelper
import chat.sphinx.test.features.authentication.core.TestEncryptionKeyHandler
import chat.sphinx.utils.build_config.BuildConfigDebug
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Cache
import okio.base64.decodeBase64ToArray
import org.cryptonode.jncryptor.AES256JNCryptor
import kotlin.jvm.JvmStatic
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * This class uses a test account setup on SphinxRelay to help ensure API compatibility.
 *
 * It is important that all tests related to use of this class **not** fail
 * if environment variables are not set.
 *
 * Wrapping tests in [getCredentials]?.let { credentials -> // my test } ensures
 * that test will simply notify that environment variables should be set with
 * their own test account credentials.
 * */
@Suppress("BlockingMethodInNonBlockingContext")
abstract class NetworkQueryTestHelper: AuthenticationCoreDefaultsTestHelper() {

    init {
        setupClassNetworkQueryTestHelper()
    }
    companion object {
        protected var privKey: String? = null
        protected var pubKey: String? = null
        protected var relayUrl: RelayUrl? = null
        protected var authorizationToken: AuthorizationToken? = null

        @JvmStatic
        fun setupClassNetworkQueryTestHelper() {
            System.getenv("SPHINX_CHAT_KEY_EXPORT")?.let { export ->
                System.getenv("SPHINX_CHAT_EXPORT_PASS")?.toCharArray()?.let { pass ->
                    setProperties(export, pass)
                    return
                }
            }

            println("\n\n***********************************************")
            println("          SPHINX_CHAT_KEY_EXPORT")
            println("                   and")
            println("          SPHINX_CHAT_EXPORT_PASS\n")
            println("    System environment variables are not set\n")
            println("        Network Tests will not be run!!!")
            println("***********************************************\n\n")
        }

        fun setProperties(keyExport: String, password: CharArray) {
            keyExport
                .decodeBase64ToArray()
                ?.toString(charset("UTF-8"))
                ?.split("::")
                ?.let { decodedSplit ->
                    if (decodedSplit.elementAtOrNull(0) != "keys") {
                        return
                    }

                    decodedSplit.elementAt(1).decodeBase64ToArray()?.let { toDecrypt ->
                        val decryptedSplit = AES256JNCryptor()
                            .decryptData(toDecrypt, password)
                            .toString(charset("UTF-8"))
                            .split("::")

                        if (decryptedSplit.size != 4) {
                            return
                        }

                        privKey = decryptedSplit[0]
                        pubKey = decryptedSplit[1]
                        relayUrl = RelayUrl(decryptedSplit[2])
                        authorizationToken = AuthorizationToken(decryptedSplit[3])
                    }
                }
        }
    }

    private val testTorManager: TorManager by lazy {
        TestTorManager()
    }

    protected data class Credentials(
        val privKey: String,
        val pubKey: String,
        val relayUrl: RelayUrl,
        val jwt: AuthorizationToken,
    )

    /**
     * Will return null if the SystemProperties for:
     *  - SPHINX_CHAT_KEY_EXPORT
     *  - SPHINX_CHAT_EXPORT_PASS
     *
     * are not set, allowing for a soft failure of the tests.
     * */
    protected fun getCredentials(): Credentials? =
        privKey?.let { nnPrivKey ->
            pubKey?.let { nnPubKey ->
                relayUrl?.let { nnRelayUrl ->
                    authorizationToken?.let { nnJwt ->
                        Credentials(
                            nnPrivKey,
                            nnPubKey,
                            nnRelayUrl,
                            nnJwt
                        )
                    }
                }
            }
        }

    protected open val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private class TestSphinxLogger: SphinxLogger() {
        override fun log(tag: String, message: String, type: LogType, throwable: Throwable?) {}
    }

    protected open val testLogger: SphinxLogger by lazy {
        TestSphinxLogger()
    }

    /**
     * Override this and set to `true` to use Logging Interceptors during the test
     * */
    open val useLoggingInterceptors: Boolean = false

    @get:Rule
    val testDirectory = TemporaryFolder()

    open val okHttpCache: Cache by lazy {
        Cache(testDirectory.newFile("okhttp_test_cache"), 2000000L /*2MB*/)
    }

    protected open val networkClient: NetworkClient by lazy {
        NetworkClientImpl(
            // true will add interceptors to the OkHttpClient
            BuildConfigDebug(useLoggingInterceptors),
            okHttpCache,
            dispatchers,
            null,
            testTorManager,
            testLogger,
        )
    }

    protected open val relayDataHandler: RelayDataHandler by lazy {
        RelayDataHandlerImpl(
            testStorage,
            testCoreManager,
            dispatchers,
            testHandler,
            testTorManager,
        )
    }

    protected open val networkRelayCall: NetworkRelayCall by lazy {
        NetworkRelayCallImpl(
            dispatchers,
            moshi,
            networkClient,
            relayDataHandler,
            testLogger
        )
    }

    protected open val nqChat: NetworkQueryChat by lazy {
        NetworkQueryChatImpl(networkRelayCall)
    }

    protected open val nqContact: NetworkQueryContact by lazy {
        NetworkQueryContactImpl(networkRelayCall)
    }

    protected open val nqInvite: NetworkQueryInvite by lazy {
        NetworkQueryInviteImpl(networkRelayCall)
    }

    protected open val nqMessage: NetworkQueryMessage by lazy {
        NetworkQueryMessageImpl(networkRelayCall)
    }

    protected open val nqSubscription: NetworkQuerySubscription by lazy {
        NetworkQuerySubscriptionImpl(networkRelayCall)
    }

    protected open val nqVersion: NetworkQueryVersion by lazy {
        NetworkQueryVersionImpl(networkRelayCall)
    }

    protected open val nqLightning: NetworkQueryLightning by lazy {
        NetworkQueryLightningImpl(networkRelayCall)
    }

    protected open val nqAuthorizeExternal: NetworkQueryAuthorizeExternal by lazy {
        NetworkQueryAuthorizeExternalImpl(networkRelayCall)
    }

    protected open val nqSaveProfile: NetworkQuerySaveProfile by lazy {
        NetworkQuerySaveProfileImpl(networkRelayCall)
    }

    @BeforeTest
    fun setupNetworkQueryTestHelper() = testDispatcher.runBlockingTest {
        testDirectory.create()
        getCredentials()?.let { creds ->
            // Set our raw private/public keys in the test handler so when we login
            // for the first time the generated keys will be these
            testHandler.keysToRestore = TestEncryptionKeyHandler.RestoreKeyHolder(
                Password(creds.privKey.toCharArray()),
                Password(creds.pubKey.toCharArray())
            )

            // login for the first time to setup the authentication library with
            // a pin of 000000
            login()

            // persist our relay url and java web token to test storage
            relayDataHandler.persistAuthorizationToken(creds.jwt)
            relayDataHandler.persistRelayUrl(creds.relayUrl)
        }

        // if null, do nothing.
    }

    @AfterTest
    fun tearDownNetworkQueryTestHelper() {
        testDirectory.delete()
    }
}