package chat.sphinx.authentication.model

import chat.sphinx.wrapper.lightning.LightningNodePubKey

data class OnBoardInviterData(
    val nickname: String?,
    val pubkey: LightningNodePubKey?,
    val routeHint: String?,
    val message: String?,
    val action: String?,
    val pin: String?
)
