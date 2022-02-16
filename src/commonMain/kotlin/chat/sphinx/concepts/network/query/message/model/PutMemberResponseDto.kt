package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PutMemberResponseDto(
    val chat: ChatDto,
    val message: MessageDto,
)