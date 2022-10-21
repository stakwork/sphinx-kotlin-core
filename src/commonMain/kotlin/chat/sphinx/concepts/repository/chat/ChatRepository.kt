package chat.sphinx.concepts.repository.chat

import chat.sphinx.concepts.network.query.chat.model.ChatDto
import chat.sphinx.concepts.network.query.chat.model.TribeDto
import chat.sphinx.concepts.repository.chat.model.CreateTribe
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.chat.ChatAlias
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.chat.TribeData
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.meme_server.PublicAttachmentInfo
import chat.sphinx.wrapper.podcast.Podcast
import chat.sphinx.wrapper_chat.NotificationLevel
import kotlinx.coroutines.flow.Flow

/**
 * All [Chat]s are cached to the DB such that a network refresh will update
 * them, and thus proc any [Flow] being collected
 * */
interface ChatRepository {
    suspend fun getChatById(chatId: ChatId): Chat?
    fun getChatByIdFlow(chatId: ChatId): Flow<Chat?>
    fun getChatByUUID(chatUUID: ChatUUID): Flow<Chat?>
    fun getPodcastByChatId(chatId: ChatId): Flow<Podcast?>

    val networkRefreshChatsFlow: Flow<LoadResponse<Boolean, ResponseError>>

    suspend fun getAllChatsByIds(chatIds: List<ChatId>): List<Chat>
    /**
     * Returns `true` if the user has muted the chat and there is a need
     * to notify them that they won't receive messages anymore.
     *
     * Returns `false` if the user has _un_ muted the chat and there is no
     * need to notify.
     *
     * Returns error if something went wrong (networking)
     * */
    suspend fun setNotificationLevel(chat: Chat, level: NotificationLevel): Response<Boolean, ResponseError>

    suspend fun updateChatContentSeenAt(chatId: ChatId)

    fun joinTribe(
        tribeDto: TribeDto,
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    suspend fun updateTribeInfo(chat: Chat): TribeData?
    suspend fun createTribe(createTribe: CreateTribe): Response<Any, ResponseError>
    suspend fun updateTribe(chatId: ChatId, createTribe: CreateTribe): Response<Any, ResponseError>
    suspend fun exitAndDeleteTribe(chat: Chat): Response<Boolean, ResponseError>

    suspend fun updateChatProfileInfo(
        chatId: ChatId,
        alias: ChatAlias? = null,
        profilePic: PublicAttachmentInfo? = null,
    ): Response<ChatDto, ResponseError>

    suspend fun kickMemberFromTribe(chatId: ChatId, contactId: ContactId): Response<Any, ResponseError>
}
