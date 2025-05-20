package chat.sphinx.concepts.repository.chat

import chat.sphinx.concepts.network.query.chat.model.NewTribeDto
import chat.sphinx.concepts.repository.chat.model.CreateTribe
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.*
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.lsat.Lsat
import chat.sphinx.wrapper.lsat.LsatIdentifier
import chat.sphinx.wrapper.lsat.LsatIssuer
import chat.sphinx.wrapper.lsat.LsatStatus
import chat.sphinx.wrapper.meme_server.PublicAttachmentInfo
import chat.sphinx.wrapper.message.Message
import chat.sphinx.wrapper.message.RemoteTimezoneIdentifier
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

    suspend fun updateTribeInfo(chat: Chat, isProductionEnvironment: Boolean): NewTribeDto?
    suspend fun storeTribe(createTribe: CreateTribe, chatId: ChatId?)
    suspend fun updateTribe(chatId: ChatId, createTribe: CreateTribe): Response<Any, ResponseError>

    suspend fun exitAndDeleteTribe(tribe: Chat)

    suspend fun updateChatProfileInfo(
        chatId: ChatId,
        alias: ChatAlias? = null,
        profilePic: PublicAttachmentInfo? = null,
    )

    suspend fun togglePinMessage(
        chatId: ChatId,
        message: Message,
        isUnpinMessage: Boolean,
        errorMessage: String,
        isProductionEnvironment: Boolean
    ): Response<Any, ResponseError>


    suspend fun updateTimezoneEnabledStatus(
        isTimezoneEnabled: TimezoneEnabled,
        chatId: ChatId
    )

    suspend fun updateTimezoneIdentifier(
        timezoneIdentifier: TimezoneIdentifier?,
        chatId: ChatId
    )

    suspend fun updateTimezoneUpdated(
        timezoneUpdated: TimezoneUpdated,
        chatId: ChatId
    )

    suspend fun updateTimezoneUpdatedOnSystemChange()

    suspend fun updateChatRemoteTimezoneIdentifier(
        remoteTimezoneIdentifier: RemoteTimezoneIdentifier?,
        chatId: ChatId,
        isRestore: Boolean
    )

    suspend fun getLastLsatByIssuer(issuer: LsatIssuer): Flow<Lsat?>
    suspend fun getLastLsatActive(): Flow<Lsat?>

    suspend fun getLsatByIdentifier(identifier: LsatIdentifier): Flow<Lsat?>
    suspend fun upsertLsat(lsat: Lsat)
    suspend fun updateLsatStatus(identifier: LsatIdentifier, status: LsatStatus)

}
