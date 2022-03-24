package chat.sphinx.di.container

import chat.sphinx.di.networkCache
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
import chat.sphinx.features.network.query.verify_external.NetworkQueryAuthorizeExternalImpl
import chat.sphinx.features.network.query.version.NetworkQueryVersionImpl
import chat.sphinx.features.network.relay_call.NetworkRelayCallImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
import chat.sphinx.features.socket_io.SocketIOManagerImpl
import chat.sphinx.utils.createTorManager
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.relay.AuthorizationToken
import okhttp3.Cache

class NetworkModule(
    appModule: AppModule,
    authenticationModule: AuthenticationModule
) {
    val torManager = createTorManager(
        appModule.applicationScope,
        authenticationModule.authenticationStorage,
        appModule.buildConfigDebug,
        appModule.buildConfigVersionCode,
        appModule.dispatchers,
        appModule.sphinxLogger
    )
    private val relayDataHandlerImpl = RelayDataHandlerImpl(
        authenticationModule.authenticationStorage,
        authenticationModule.authenticationCoreManager,
        appModule.dispatchers,
        authenticationModule.encryptionKeyHandler,
        torManager
    )
    val relayDataHandler = relayDataHandlerImpl
    private val networkClientImpl = NetworkClientImpl(
        appModule.buildConfigDebug,
        Cache.networkCache(),
        appModule.dispatchers,
        NetworkClientImpl.RedactedLoggingHeaders(
            listOf(
                AuthorizationToken.AUTHORIZATION_HEADER,
                AuthenticationToken.HEADER_KEY
            )
        ),
        torManager,
        appModule.sphinxLogger,
    )
    val networkClient = networkClientImpl
    val networkClientCache = networkClientImpl
    private val socketIOManagerImpl = SocketIOManagerImpl(
        appModule.dispatchers,
        networkClient,
        relayDataHandler,
        appModule.sphinxLogger,
    )
    val socketIOManager = socketIOManagerImpl
    private val networkRelayCallImpl = NetworkRelayCallImpl(
        appModule.dispatchers,
        networkClient,
        relayDataHandler,
        appModule.sphinxLogger
    )
    val networkRelayCall = networkRelayCallImpl
    val networkCall = networkRelayCallImpl
    private val networkQueryChatImpl = NetworkQueryChatImpl(networkRelayCall)
    val networkQueryChat = networkQueryChatImpl
    val linkPreviewHandler = LinkPreviewHandlerImpl(
        appModule.dispatchers,
        networkClient,
        networkQueryChat
    )
    val memeInputStreamHandler = MemeInputStreamHandlerImpl(
        appModule.dispatchers,
        networkClientCache
    )
    private val networkQueryContactImpl = NetworkQueryContactImpl(networkRelayCall)
    val networkQueryContact = networkQueryContactImpl
    private val networkQueryInviteImpl = NetworkQueryInviteImpl(networkRelayCall)
    val networkQueryInvite = networkQueryInviteImpl
    private val networkQueryLightningImpl = NetworkQueryLightningImpl(networkRelayCall)
    val networkQueryLightning = networkQueryLightningImpl
    private val networkQueryMessageImpl = NetworkQueryMessageImpl(networkRelayCall)
    val networkQueryMessage = networkQueryMessageImpl
    private val networkQuerySubscriptionImpl = NetworkQuerySubscriptionImpl(networkRelayCall)
    val networkQuerySubscription = networkQuerySubscriptionImpl
    private val networkQueryMemeServerImpl = NetworkQueryMemeServerImpl(
        appModule.dispatchers,
        networkRelayCall
    )
    val networkQueryMemeServer = networkQueryMemeServerImpl
    private val networkQueryVersionImpl = NetworkQueryVersionImpl(networkRelayCall)
    val networkQueryVersion = networkQueryVersionImpl
    private val networkQueryAuthorizeExternalImpl = NetworkQueryAuthorizeExternalImpl(networkRelayCall)
    val networkQueryAuthorizeExternal = networkQueryAuthorizeExternalImpl
    private val networkQueryFeedSearchImpl = NetworkQueryFeedSearchImpl(networkRelayCall)
    val networkQueryFeedSearch = networkQueryFeedSearchImpl
    private val networkQuerySaveProfileImpl = NetworkQuerySaveProfileImpl(networkRelayCall)
    val networkQuerySaveProfile = networkQuerySaveProfileImpl
    private val networkQueryRedeemBadgeTokenImpl = NetworkQueryRedeemBadgeTokenImpl(networkRelayCall)
    val networkQueryRedeemBadgeToken = networkQueryRedeemBadgeTokenImpl
}