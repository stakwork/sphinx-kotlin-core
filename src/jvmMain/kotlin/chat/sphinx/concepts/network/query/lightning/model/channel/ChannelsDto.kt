package chat.sphinx.concepts.network.query.lightning.model.channel

import kotlinx.serialization.Serializable

@Serializable
data class ChannelsDto(val channels: List<ChannelDto>)