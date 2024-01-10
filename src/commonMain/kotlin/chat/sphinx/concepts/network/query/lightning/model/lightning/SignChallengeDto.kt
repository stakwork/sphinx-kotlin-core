package chat.sphinx.concepts.network.query.lightning.model.lightning

import kotlinx.serialization.Serializable

@Serializable
data class SignChallengeDto(
    val sig: String
)
