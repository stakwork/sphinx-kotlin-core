package chat.sphinx.authentication

import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.rsa.RsaPublicKey
import kotlinx.coroutines.flow.Flow

abstract class KeyRestore {

    /**
     * Used only by the Splash screen for restoring keys, and implemented by the application
     * such that only those 2 modules have knowledge of this functionality.
     * */
    abstract fun restoreKeys(
        privateKey: Password,
        publicKey: Password,
        userPin: CharArray,
        relayUrl: RelayUrl,
        authorizationToken: AuthorizationToken,
        transportKey: RsaPublicKey? = null
    ): Flow<KeyRestoreResponse>
}