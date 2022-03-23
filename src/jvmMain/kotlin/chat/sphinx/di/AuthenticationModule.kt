package chat.sphinx.di

import chat.sphinx.authentication.SphinxAuthenticationCoreManager
import chat.sphinx.authentication.SphinxAuthenticationCoreStorage
import chat.sphinx.authentication.SphinxEncryptionKeyHandler
import chat.sphinx.authentication.SphinxKeyRestore
import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.authentication.encryption_key.EncryptionKeyHandler
import chat.sphinx.concepts.authentication.state.AuthenticationStateManager
import chat.sphinx.concepts.background.login.BackgroundLoginHandler
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.foreground_state.ForegroundStateManager
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.features.background.login.BackgroundLoginHandlerImpl
import chat.sphinx.features.coredb.CoreDBImpl
import chat.sphinx.features.crypto_rsa.RSAAlgorithm
import chat.sphinx.features.crypto_rsa.RSAImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
import chat.sphinx.authentication.KeyRestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {

    @Provides
    @Singleton
    fun provideRSA(): RSA =
        RSAImpl(RSAAlgorithm.RSA_ECB_PKCS1Padding)

    @Provides
    fun provideAuthenticationCoreManager(
        sphinxAuthenticationCoreManager: SphinxAuthenticationCoreManager
    ): AuthenticationCoreManager =
        sphinxAuthenticationCoreManager

    @Provides
    fun provideAuthenticationStateManager(
        sphinxAuthenticationCoreManager: SphinxAuthenticationCoreManager
    ): AuthenticationStateManager =
        sphinxAuthenticationCoreManager

    @Provides
    fun provideForegroundStateManager(
        sphinxAuthenticationCoreManager: SphinxAuthenticationCoreManager
    ): ForegroundStateManager =
        sphinxAuthenticationCoreManager

    @Provides
    fun provideAuthenticationStorage(
        sphinxAuthenticationCoreStorage: SphinxAuthenticationCoreStorage
    ): AuthenticationStorage =
        sphinxAuthenticationCoreStorage

    @Provides
    fun provideEncryptionKeyHandler(
        sphinxEncryptionKeyHandler: SphinxEncryptionKeyHandler
    ): EncryptionKeyHandler =
        sphinxEncryptionKeyHandler

    @Provides
    fun provideBackgroundLoginHandler(
        authenticationCoreManager: AuthenticationCoreManager,
        authenticationStorage: AuthenticationStorage
    ): BackgroundLoginHandler =
        BackgroundLoginHandlerImpl(
            authenticationCoreManager,
            authenticationStorage
        )

    @Provides
    fun provideKeyRestore(
        sphinxKeyRestore: SphinxKeyRestore
    ): KeyRestore =
        sphinxKeyRestore

    @Provides
    @Singleton
    fun provideSphinxAuthenticationCoreStorage(
        dispatchers: CoroutineDispatchers,
    ): SphinxAuthenticationCoreStorage =
        SphinxAuthenticationCoreStorage(
            dispatchers,
        )

    @Provides
    @Singleton
    fun provideSphinxEncryptionKeyHandler(
        rsa: RSA
    ): SphinxEncryptionKeyHandler =
        SphinxEncryptionKeyHandler(rsa)

    @Provides
    @Singleton
    fun provideSphinxAuthenticationCoreManager(
        dispatchers: CoroutineDispatchers,
        encryptionKeyHandler: SphinxEncryptionKeyHandler,
        sphinxAuthenticationCoreStorage: SphinxAuthenticationCoreStorage,
        coreDBImpl: CoreDBImpl,
    ): SphinxAuthenticationCoreManager =
        SphinxAuthenticationCoreManager(
            dispatchers,
            encryptionKeyHandler,
            sphinxAuthenticationCoreStorage,
            coreDBImpl,
        )

    // _Not_ singleton (only utilized by the Splash Screen)
    @Provides
    fun provideSphinxKeyRestore(
        sphinxAuthenticationCoreManager: SphinxAuthenticationCoreManager,
        sphinxAuthenticationCoreStorage: SphinxAuthenticationCoreStorage,
        sphinxEncryptionKeyHandler: SphinxEncryptionKeyHandler,
        relayDataHandlerImpl: RelayDataHandlerImpl,
    ): SphinxKeyRestore =
        SphinxKeyRestore(
            sphinxAuthenticationCoreManager,
            sphinxAuthenticationCoreStorage,
            sphinxEncryptionKeyHandler,
            relayDataHandlerImpl
        )
}
