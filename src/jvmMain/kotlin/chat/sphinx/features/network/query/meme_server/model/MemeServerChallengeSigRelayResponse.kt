package chat.sphinx.features.network.query.meme_server.model

import chat.sphinx.concepts.network.query.meme_server.model.MemeServerChallengeSigDto
import chat.sphinx.concepts.network.relay_call.RelayResponse

import kotlinx.serialization.Serializable

@Serializable
internal data class MemeServerChallengeSigRelayResponse(
    override val success: Boolean,
    override val response: MemeServerChallengeSigDto?,
    override val error: String?
) : RelayResponse<MemeServerChallengeSigDto>()
