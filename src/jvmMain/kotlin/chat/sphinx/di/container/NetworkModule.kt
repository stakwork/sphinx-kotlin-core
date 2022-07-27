package chat.sphinx.di.container

import chat.sphinx.concepts.link_preview.LinkPreviewHandler
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.network.call.NetworkCall
import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.client.cache.NetworkClientCache
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.invite.NetworkQueryInvite
import chat.sphinx.concepts.network.query.lightning.NetworkQueryLightning
import chat.sphinx.concepts.network.query.meme_server.NetworkQueryMemeServer
import chat.sphinx.concepts.network.query.message.NetworkQueryMessage
import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.subscription.NetworkQuerySubscription
import chat.sphinx.concepts.network.query.relay_keys.NetworkQueryRelayKeys
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.network.query.version.NetworkQueryVersion
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.concepts.socket_io.SocketIOManager
import chat.sphinx.features.link_preview.LinkPreviewHandlerImpl
import chat.sphinx.features.meme_input_stream.MemeInputStreamHandlerImpl
import chat.sphinx.features.network.client.NetworkClientImpl
import chat.sphinx.features.network.query.chat.NetworkQueryChatImpl
import chat.sphinx.features.network.query.contact.NetworkQueryContactImpl
import chat.sphinx.features.network.query.feed_search.NetworkQueryFeedSearchImpl
import chat.sphinx.features.network.query.invite.NetworkQueryInviteImpl
import chat.sphinx.features.network.query.lightning.NetworkQueryLightningImpl
import chat.sphinx.features.network.query.meme_server.NetworkQueryMemeServerImpl
import chat.sphinx.features.network.query.message.NetworkQueryMessageImpl
import chat.sphinx.features.network.query.redeem_badge_token.NetworkQueryRedeemBadgeTokenImpl
import chat.sphinx.features.network.query.save_profile.NetworkQuerySaveProfileImpl
import chat.sphinx.features.network.query.subscription.NetworkQuerySubscriptionImpl
import chat.sphinx.features.network.query.transport_key.NetworkQueryRelayKeysImpl
import chat.sphinx.features.network.query.verify_external.NetworkQueryAuthorizeExternalImpl
import chat.sphinx.features.network.query.version.NetworkQueryVersionImpl
import chat.sphinx.features.network.relay_call.NetworkRelayCallImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
import chat.sphinx.features.socket_io.SocketIOManagerImpl
import chat.sphinx.utils.createTorManager
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.TransportToken
import okhttp3.Cache
import okio.FileSystem

/**
 * TODO: Get cache through alternative means...
 */
fun Cache.Companion.networkCache(): Cache {
    return Cache(
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("networkCache").toFile(),
        22
    )
}

class NetworkModule(
    appModule: AppModule,
    authenticationModule: AuthenticationModule
) {
    private val torManager = createTorManager(
        appModule.applicationScope,
        authenticationModule.authenticationStorage,
        appModule.buildConfigDebug,
        appModule.buildConfigVersionCode,
        appModule.dispatchers,
        appModule.sphinxLogger
    )
    val relayDataHandlerImpl = RelayDataHandlerImpl(
        authenticationModule.authenticationStorage,
        authenticationModule.authenticationCoreManager,
        appModule.dispatchers,
        authenticationModule.encryptionKeyHandler,
        torManager,
        authenticationModule.rsa
    )
    val relayDataHandler: RelayDataHandler = relayDataHandlerImpl
    private val networkClientImpl = NetworkClientImpl(
        appModule.buildConfigDebug,
        Cache.networkCache(),
        appModule.dispatchers,
        NetworkClientImpl.RedactedLoggingHeaders(
            listOf(
                AuthorizationToken.AUTHORIZATION_HEADER,
                TransportToken.TRANSPORT_TOKEN_HEADER,
                AuthenticationToken.HEADER_KEY
            )
        ),
        torManager,
        appModule.sphinxLogger,
    )
    private val networkClient: NetworkClient = networkClientImpl
    private val networkClientCache: NetworkClientCache = networkClientImpl
    private val socketIOManagerImpl = SocketIOManagerImpl(
        appModule.dispatchers,
        networkClient,
        relayDataHandler,
        appModule.sphinxLogger,
    )
    val socketIOManager: SocketIOManager = socketIOManagerImpl
    private val networkRelayCallImpl = NetworkRelayCallImpl(
        appModule.dispatchers,
        networkClient,
        relayDataHandler,
        appModule.sphinxLogger
    )
    val networkRelayCall: NetworkRelayCall = networkRelayCallImpl
    val networkCall: NetworkCall = networkRelayCallImpl
    private val networkQueryChatImpl = NetworkQueryChatImpl(networkRelayCall)
    val networkQueryChat: NetworkQueryChat = networkQueryChatImpl
    val linkPreviewHandler: LinkPreviewHandler = LinkPreviewHandlerImpl(
        appModule.dispatchers,
        networkClient,
        networkQueryChat
    )
    val memeInputStreamHandler: MemeInputStreamHandler = MemeInputStreamHandlerImpl(
        appModule.dispatchers,
        networkClientCache
    )
    private val networkQueryContactImpl = NetworkQueryContactImpl(networkRelayCall)
    val networkQueryContact: NetworkQueryContact = networkQueryContactImpl

    private val networkQueryInviteImpl = NetworkQueryInviteImpl(networkRelayCall)
    val networkQueryInvite: NetworkQueryInvite = networkQueryInviteImpl

    private val networkQueryLightningImpl = NetworkQueryLightningImpl(networkRelayCall)
    val networkQueryLightning: NetworkQueryLightning = networkQueryLightningImpl

    private val networkQueryMessageImpl = NetworkQueryMessageImpl(networkRelayCall)
    val networkQueryMessage: NetworkQueryMessage = networkQueryMessageImpl

    private val networkQuerySubscriptionImpl = NetworkQuerySubscriptionImpl(networkRelayCall)
    val networkQuerySubscription: NetworkQuerySubscription = networkQuerySubscriptionImpl

    private val networkQueryMemeServerImpl = NetworkQueryMemeServerImpl(
        appModule.dispatchers,
        networkRelayCall
    )
    val networkQueryMemeServer: NetworkQueryMemeServer = networkQueryMemeServerImpl

    private val networkQueryVersionImpl = NetworkQueryVersionImpl(networkRelayCall)
    val networkQueryVersion: NetworkQueryVersion = networkQueryVersionImpl

    private val networkQueryAuthorizeExternalImpl = NetworkQueryAuthorizeExternalImpl(networkRelayCall)
    val networkQueryAuthorizeExternal: NetworkQueryAuthorizeExternal = networkQueryAuthorizeExternalImpl

    private val networkQueryFeedSearchImpl = NetworkQueryFeedSearchImpl(networkRelayCall)
    val networkQueryFeedSearch: NetworkQueryFeedSearch = networkQueryFeedSearchImpl

    private val networkQuerySaveProfileImpl = NetworkQuerySaveProfileImpl(networkRelayCall)
    val networkQuerySaveProfile: NetworkQuerySaveProfile = networkQuerySaveProfileImpl

    private val networkQueryRedeemBadgeTokenImpl = NetworkQueryRedeemBadgeTokenImpl(networkRelayCall)
    val networkQueryRedeemBadgeToken: NetworkQueryRedeemBadgeToken = networkQueryRedeemBadgeTokenImpl

    private val networkQueryRelayKeysImpl = NetworkQueryRelayKeysImpl(networkRelayCall)
    val networkQueryRelayKeys: NetworkQueryRelayKeys = networkQueryRelayKeysImpl
}