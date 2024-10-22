package chat.sphinx.di.container

import chat.sphinx.authentication.SphinxAuthenticationCoreManager
import chat.sphinx.authentication.SphinxAuthenticationCoreStorage
import chat.sphinx.authentication.SphinxEncryptionKeyHandler
import chat.sphinx.authentication.SphinxKeyRestore
import chat.sphinx.concepts.authentication.coordinator.AuthenticationCoordinator
import chat.sphinx.features.authentication.core.AuthenticationCoreCoordinator
import chat.sphinx.features.background.login.BackgroundLoginHandlerImpl
import chat.sphinx.features.crypto_rsa.RSAAlgorithm
import chat.sphinx.features.crypto_rsa.RSAImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl

class AuthenticationModule(
    appModule: AppModule,
) {
    val sphinxAuthenticationCoreStorage = SphinxAuthenticationCoreStorage(
        appModule.dispatchers
    )
    val rsa = RSAImpl(RSAAlgorithm.RSA_ECB_PKCS1Padding)
    private val sphinxEncryptionKeyHandler = SphinxEncryptionKeyHandler(rsa)
    val encryptionKeyHandler = sphinxEncryptionKeyHandler
    private val sphinxAuthenticationCoreManager = SphinxAuthenticationCoreManager(
            appModule.dispatchers,
            encryptionKeyHandler,
            sphinxAuthenticationCoreStorage,
            appModule.coreDBImpl,
        )
    val sphinxAuthenticationCoreCoordinator = object : AuthenticationCoreCoordinator(sphinxAuthenticationCoreManager) {
        override suspend fun navigateToAuthenticationView() {
            // Provide the necessary navigation implementation here, e.g., routing to an authentication UI
        }
    }
    val authenticationCoreManager = sphinxAuthenticationCoreManager
    val authenticationCoreCoordinator = sphinxAuthenticationCoreCoordinator
    val authenticationStateManager = sphinxAuthenticationCoreManager
    val foregroundStateManager = sphinxAuthenticationCoreManager
    val authenticationStorage = sphinxAuthenticationCoreStorage
    val backgroundLoginHandler = BackgroundLoginHandlerImpl(
        authenticationCoreManager,
        authenticationStorage
    )


    fun sphinxKeyRestore(
        relayDataHandlerImpl: RelayDataHandlerImpl
    ): SphinxKeyRestore = SphinxKeyRestore(
        sphinxAuthenticationCoreManager,
        sphinxAuthenticationCoreStorage,
        sphinxEncryptionKeyHandler,
        relayDataHandlerImpl
    )

    fun keyRestore(
        relayDataHandlerImpl: RelayDataHandlerImpl
    ): SphinxKeyRestore = sphinxKeyRestore(relayDataHandlerImpl)
}