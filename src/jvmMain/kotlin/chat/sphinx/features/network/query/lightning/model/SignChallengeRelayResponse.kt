package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.lightning.ActiveLsatDto
import chat.sphinx.concepts.network.query.lightning.model.lightning.SignChallengeDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class SignChallengeRelayResponse(
    override val success: Boolean,
    override val response: SignChallengeDto? = null,
    override val error: String? = null
): RelayResponse<SignChallengeDto>()
