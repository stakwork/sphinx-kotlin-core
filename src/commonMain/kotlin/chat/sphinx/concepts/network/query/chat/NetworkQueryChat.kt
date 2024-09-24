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
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import chat.sphinx.wrapper_chat.NotificationLevel
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryChat {

    abstract fun getTribeInfo(
        host: ChatHost,
        tribePubKey: LightningNodePubKey,
        isProductionEnvironment: Boolean,
    ): Flow<LoadResponse<NewTribeDto, ResponseError>>

    abstract fun getFeedContent(
        host: ChatHost,
        feedUrl: FeedUrl,
        chatUUID: ChatUUID?,
    ): Flow<LoadResponse<FeedDto, ResponseError>>
}
