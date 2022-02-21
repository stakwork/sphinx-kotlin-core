package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import kotlinx.serialization.Serializable

@Serializable
data class PutMemberResponseDto(
    val chat: ChatDto,
    val message: MessageDto,
)