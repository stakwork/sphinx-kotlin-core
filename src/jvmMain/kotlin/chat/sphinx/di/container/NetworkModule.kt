package chat.sphinx.di.container

import chat.sphinx.concepts.link_preview.LinkPreviewHandler
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.network.call.NetworkCall
import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.client.cache.NetworkClientCache
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.meme_server.NetworkQueryMemeServer
import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.features.link_preview.LinkPreviewHandlerImpl
import chat.sphinx.features.meme_input_stream.MemeInputStreamHandlerImpl
import chat.sphinx.features.network.client.NetworkClientImpl
import chat.sphinx.features.network.query.chat.NetworkQueryChatImpl
import chat.sphinx.features.network.query.contact.NetworkQueryContactImpl
import chat.sphinx.features.network.query.feed_search.NetworkQueryFeedSearchImpl
import chat.sphinx.features.network.query.meme_server.NetworkQueryMemeServerImpl
import chat.sphinx.features.network.query.redeem_badge_token.NetworkQueryRedeemBadgeTokenImpl
import chat.sphinx.features.network.query.save_profile.NetworkQuerySaveProfileImpl
import chat.sphinx.features.network.query.verify_external.NetworkQueryAuthorizeExternalImpl
import chat.sphinx.features.network.relay_call.NetworkRelayCallImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
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
        appModule.applicationScope,
        appModule.buildConfigDebug,
        Cache.networkCache(),
        appModule.dispatchers,
        authenticationModule.authenticationStorage,
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
    val networkClient: NetworkClient = networkClientImpl
    private val networkClientCache: NetworkClientCache = networkClientImpl
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

    private val networkQueryMemeServerImpl = NetworkQueryMemeServerImpl(
        appModule.dispatchers,
        networkRelayCall
    )
    val networkQueryMemeServer: NetworkQueryMemeServer = networkQueryMemeServerImpl

    private val networkQueryAuthorizeExternalImpl = NetworkQueryAuthorizeExternalImpl(networkRelayCall)
    val networkQueryAuthorizeExternal: NetworkQueryAuthorizeExternal = networkQueryAuthorizeExternalImpl

    private val networkQueryFeedSearchImpl = NetworkQueryFeedSearchImpl(networkRelayCall)
    val networkQueryFeedSearch: NetworkQueryFeedSearch = networkQueryFeedSearchImpl

    private val networkQuerySaveProfileImpl = NetworkQuerySaveProfileImpl(networkRelayCall)
    val networkQuerySaveProfile: NetworkQuerySaveProfile = networkQuerySaveProfileImpl

    private val networkQueryRedeemBadgeTokenImpl = NetworkQueryRedeemBadgeTokenImpl(networkRelayCall)
    val networkQueryRedeemBadgeToken: NetworkQueryRedeemBadgeToken = networkQueryRedeemBadgeTokenImpl
}