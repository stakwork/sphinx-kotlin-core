package chat.sphinx.authentication

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.concepts.authentication.state.AuthenticationState
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.foreground_state.ForegroundState
import chat.sphinx.crypto.common.clazzes.HashIterations
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.features.authentication.core.components.AuthenticationManagerInitializer
import chat.sphinx.features.coredb.CoreDBImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SphinxAuthenticationCoreManager(
    dispatchers: CoroutineDispatchers,
    encryptionKeyHandler: SphinxEncryptionKeyHandler,
    sphinxAuthenticationCoreStorage: SphinxAuthenticationCoreStorage,
    private val sphinxCoreDBImpl: CoreDBImpl,
): AuthenticationCoreManager(
    dispatchers,
    HashIterations(250_000),
    encryptionKeyHandler,
    sphinxAuthenticationCoreStorage,
    AuthenticationManagerInitializer(
        minimumUserInputLength = 6,
        maximumUserInputLength = 6,
        wrongPinAttemptsUntilLockedOut = 0,
        wrongPinLockoutDuration = 0
    )
) {
    @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
    private val _foregroundStateFlow: MutableStateFlow<ForegroundState> by lazy {
        MutableStateFlow<ForegroundState>(ForegroundState.Background)
    }

    override val foregroundStateFlow: StateFlow<ForegroundState>
        get() = _foregroundStateFlow.asStateFlow()

    val logOutWhenApplicationIsClearedFromRecentsTray: Boolean = false

    override fun onInitialLoginSuccess(encryptionKey: EncryptionKey) {
        super.onInitialLoginSuccess(encryptionKey)
        sphinxCoreDBImpl.initializeDatabase(encryptionKey)
    }

    fun logOut() {
        setAuthenticationStateRequired(AuthenticationState.Required.InitialLogIn)
    }

}
