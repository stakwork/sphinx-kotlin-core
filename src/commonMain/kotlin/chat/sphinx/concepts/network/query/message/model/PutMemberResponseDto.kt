package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import kotlinx.serialization.Serializable

@Serializable
data class PutMemberResponseDto(
    val chat: chat.sphinx.concepts.network.query.chat.model.ChatDto,
    val message: MessageDto,
)