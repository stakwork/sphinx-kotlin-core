package chat.sphinx.concepts.repository.connect_manager.model

sealed class OwnerRegistrationState {

    object OwnerRegistered: OwnerRegistrationState()
    data class MnemonicWords(val words: String): OwnerRegistrationState()
    data class SignedChallenge(val authToken: String): OwnerRegistrationState()
    data class NewInviteCode(val inviteCode: String): OwnerRegistrationState()
    object GetNodes: OwnerRegistrationState()
    data class StoreRouterPubKey(val nodes: String): OwnerRegistrationState()
}
