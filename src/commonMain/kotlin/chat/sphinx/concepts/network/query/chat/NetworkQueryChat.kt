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
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryChat {

    ///////////
    /// GET ///
    ///////////
    abstract fun getChats(
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<List<chat.sphinx.concepts.network.query.chat.model.ChatDto>, ResponseError>>

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
        putChatDto: chat.sphinx.concepts.network.query.chat.model.PutChatDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto, ResponseError>>

//    app.put('/chat/:id', chats.addGroupMembers)
//    app.put('/member/:contactId/:status/:messageId', chatTribes.approveOrRejectMember)

    abstract fun kickMemberFromChat(
        chatId: ChatId,
        contactId: ContactId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto, ResponseError>>

    abstract fun updateTribe(
        chatId: ChatId,
        postGroupDto: chat.sphinx.concepts.network.query.chat.model.PostGroupDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto, ResponseError>>

    ////////////
    /// POST ///
    ////////////
    abstract fun createTribe(
        postGroupDto: chat.sphinx.concepts.network.query.chat.model.PostGroupDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto?, ResponseError>>

    abstract fun streamSats(
        postStreamSatsDto: chat.sphinx.concepts.network.query.chat.model.PostStreamSatsDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<Any?, ResponseError>>

    abstract fun toggleMuteChat(
        chatId: ChatId,
        muted: ChatMuted,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto, ResponseError>>

    abstract fun joinTribe(
        tribeDto: TribeDto,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<chat.sphinx.concepts.network.query.chat.model.ChatDto, ResponseError>>

    /**
     * Returns a map of "chat_id": chatId
     * */
    abstract suspend fun deleteChat(
        chatId: ChatId,
        relayData: Triple<AuthorizationToken, TransportToken?, RelayUrl>? = null
    ): Flow<LoadResponse<Map<String, Long>, ResponseError>>
}
