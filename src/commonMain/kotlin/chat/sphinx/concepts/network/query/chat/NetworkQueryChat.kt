package chat.sphinx.concepts.network.query.chat

import chat.sphinx.concepts.network.query.chat.model.*
import chat.sphinx.concepts.network.query.chat.model.feed.FeedDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatMuted
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.feed.FeedUrl
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import chat.sphinx.wrapper_chat.NotificationLevel
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryChat {

    ///////////
    /// GET ///
    ///////////
    abstract fun getChats(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<List<ChatDto>, ResponseError>>

    abstract fun getTribeInfo(
        host: ChatHost,
        uuid: ChatUUID
    ): Flow<LoadResponse<TribeDto, ResponseError>>

    abstract fun getFeedContent(
        host: ChatHost,
        feedUrl: FeedUrl,
        chatUUID: ChatUUID?,
    ): Flow<LoadResponse<FeedDto, ResponseError>>

    ///////////
    /// PUT ///
    ///////////
    abstract fun updateChat(
        chatId: ChatId,
        putChatDto: PutChatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

//    app.put('/chat/:id', chats.addGroupMembers)
//    app.put('/member/:contactId/:status/:messageId', chatTribes.approveOrRejectMember)

    abstract fun kickMemberFromChat(
        chatId: ChatId,
        contactId: ContactId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    abstract fun updateTribe(
        chatId: ChatId,
        postGroupDto: PostGroupDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    ////////////
    /// POST ///
    ////////////
    abstract fun createTribe(
        postGroupDto: PostGroupDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto?, ResponseError>>

    abstract fun streamSats(
        postStreamSatsDto: PostStreamSatsDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any?, ResponseError>>

    abstract fun toggleMuteChat(
        chatId: ChatId,
        muted: ChatMuted,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    abstract fun setNotificationLevel(
        chatId: ChatId,
        notificationLevel: NotificationLevel,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    abstract fun joinTribe(
        tribeDto: TribeDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ChatDto, ResponseError>>

    /**
     * Returns a map of "chat_id": chatId
     * */
    abstract suspend fun deleteChat(
        chatId: ChatId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Map<String, Long>, ResponseError>>
}
