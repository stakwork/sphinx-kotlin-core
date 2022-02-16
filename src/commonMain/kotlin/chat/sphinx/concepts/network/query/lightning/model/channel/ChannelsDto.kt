package chat.sphinx.concepts.network.query.lightning.model.channel

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChannelsDto(val channels: List<ChannelDto>)