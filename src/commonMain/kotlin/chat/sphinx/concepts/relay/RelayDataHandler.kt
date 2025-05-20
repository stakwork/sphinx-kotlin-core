package chat.sphinx.concepts.relay

import chat.sphinx.wrapper.lightning.WalletMnemonic
import chat.sphinx.wrapper.relay.*

/**
 * Persists and retrieves Sphinx Relay data to device storage. Implementation
 * requires User to be logged in to work, otherwise `null` and `false` are always
 * returned.
 * */
abstract class RelayDataHandler {
    abstract suspend fun persistWalletMnemonic(mnemonic: WalletMnemonic): Boolean
    abstract suspend fun retrieveWalletMnemonic(): WalletMnemonic?

    /**
     * Send `null` to clear the token from persistent storage
     * */
    abstract suspend fun persistAuthorizationToken(token: AuthorizationToken?): Boolean
    abstract suspend fun retrieveAuthorizationToken(): AuthorizationToken?

    abstract fun formatRelayUrl(relayUrl: RelayUrl): RelayUrl
}
