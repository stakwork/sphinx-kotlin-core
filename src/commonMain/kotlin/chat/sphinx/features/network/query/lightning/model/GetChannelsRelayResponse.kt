package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.channel.ChannelsDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetChannelsRelayResponse(
    override val success: Boolean,
    override val response: ChannelsDto?,
    override val error: String?
): RelayResponse<ChannelsDto>()
