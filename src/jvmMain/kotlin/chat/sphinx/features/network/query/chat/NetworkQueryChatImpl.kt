package chat.sphinx.features.network.query.chat

import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.chat.model.*
import chat.sphinx.concepts.network.query.chat.model.feed.FeedDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.chat.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatMuted
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.chat.isTrue
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.feed.FeedUrl
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class NetworkQueryChatImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryChat() {

    companion object {
        private const val ENDPOINT_CHAT = "/chat"
        private const val ENDPOINT_CHATS = "/chats"
        private const val ENDPOINT_EDIT_CHAT = "$ENDPOINT_CHATS/%d"
        private const val ENDPOINT_DELETE_CHAT = "$ENDPOINT_CHAT/%d"
        private const val ENDPOINT_MUTE_CHAT = "/chats/%d/%s"
        private const val MUTE_CHAT = "mute"
        private const val UN_MUTE_CHAT = "unmute"
        private const val ENDPOINT_GROUP = "/group"
        private const val ENDPOINT_EDIT_GROUP = "/group/%d"
        private const val ENDPOINT_KICK = "/kick/%d/%d"
        private const val ENDPOINT_MEMBER = "/member"
        private const val ENDPOINT_TRIBE = "/tribe"
        private const val ENDPOINT_STREAM_SATS = "/stream"

        private const val GET_TRIBE_INFO_URL = "https://%s/tribes/%s"
        private const val GET_FEED_CONTENT_URL = "https://%s/feed?url=%s"
    }

    ///////////
    /// GET ///
    ///////////
    private val getChatsFlowNullData: Flow<LoadResponse<List<ChatDto>, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonSerializer = GetChatsRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_CHATS,
            relayData = null
        )
    }

    override fun getChats(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<List<ChatDto>, ResponseError>> =
        if (relayData == null) {
            getChatsFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonSerializer = GetChatsRelayResponse.serializer(),
                relayEndpoint = ENDPOINT_CHATS,
                relayData = relayData
            )
        }

    override fun getTribeInfo(
        host: ChatHost,
        uuid: ChatUUID
    ): Flow<LoadResponse<TribeDto, ResponseError>> =
        networkRelayCall.get(
            url = String.format(GET_TRIBE_INFO_URL, host.value, uuid.value),
            responseJsonSerializer = TribeDto.serializer(),
        )

    override fun getFeedContent(
        host: ChatHost,
        feedUrl: FeedUrl,
        chatUUID: ChatUUID?,
    ): Flow<LoadResponse<FeedDto, ResponseError>> =
        networkRelayCall.get(
            url = if (chatUUID != null) {
                "${String.format(GET_FEED_CONTENT_URL, host.value, feedUrl.value)}&uuid=${chatUUID.value}"
            } else {
                String.format(GET_FEED_CONTENT_URL, host.value, feedUrl.value)
            },
            responseJsonSerializer = FeedDto.serializer(),
        )


    ///////////
    /// PUT ///
    ///////////
    override fun updateChat(
        chatId: ChatId,
        putChatDto: PutChatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonSerializer = UpdateChatRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_EDIT_CHAT, chatId.value),
            requestBodyPair = Pair(
                putChatDto,
                PutChatDto.serializer()
            ),
            relayData = relayData
        )

    override fun kickMemberFromChat(
        chatId: ChatId,
        contactId: ContactId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>>  =
        networkRelayCall.relayPut(
            responseJsonSerializer = UpdateChatRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_KICK, chatId.value, contactId.value),
            requestBodyPair = Pair(
                mapOf(Pair("", "")),
                Json.serializersModule.serializer()
            ),
            relayData = relayData
        )

    override fun updateTribe(
    chatId: ChatId,
    postGroupDto: PostGroupDto,
    relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonSerializer = PostGroupRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_EDIT_GROUP, chatId.value),
            requestBodyPair = Pair(
                postGroupDto,
                PostGroupDto.serializer()
            ),
            relayData = relayData
        )

    ////////////
    /// POST ///
    ////////////
    override fun createTribe(
        postGroupDto: PostGroupDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto?, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = PostGroupRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_GROUP,
            requestBodyPair = Pair(
                postGroupDto,
                PostGroupDto.serializer()
            ),
            relayData = relayData
        )

    override fun streamSats(
        postStreamSatsDto: PostStreamSatsDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<Any?, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = StreamSatsRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_STREAM_SATS,
            requestBodyPair = Pair(
                postStreamSatsDto,
                PostStreamSatsDto.serializer()
            ),
            relayData = relayData
        )

    override fun toggleMuteChat(
        chatId: ChatId,
        muted: ChatMuted,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>> =
        toggleMuteChatImpl(
            endpoint = String.format(ENDPOINT_MUTE_CHAT, chatId.value, (if (muted.isTrue()) UN_MUTE_CHAT else MUTE_CHAT)),
            relayData = relayData
        )

    private fun toggleMuteChatImpl(
        endpoint: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = UpdateChatRelayResponse.serializer(),
            relayEndpoint = endpoint,
            requestBodyPair = Pair(
                mapOf(Pair("", "")),
                Json.serializersModule.serializer()
            ),
            relayData = relayData
        )

    override fun joinTribe(
        tribeDto: TribeDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<ChatDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = JoinTribeRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_TRIBE,
            requestBodyPair = Pair(
                tribeDto,
                TribeDto.serializer()
            ),
            relayData = relayData
        )

    override suspend fun deleteChat(
        chatId: ChatId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<Map<String, Long>, ResponseError>> =
        networkRelayCall.relayDelete(
            responseJsonSerializer = DeleteChatRelayResponse.serializer(),
            relayEndpoint = String.format(ENDPOINT_DELETE_CHAT, chatId.value),
            relayData = relayData,
        )
}
