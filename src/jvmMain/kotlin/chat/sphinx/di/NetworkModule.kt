package chat.sphinx.di

import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.authentication.encryption_key.EncryptionKeyHandler
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
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
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.subscription.NetworkQuerySubscription
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.network.query.version.NetworkQueryVersion
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.concepts.socket_io.SocketIOManager
import chat.sphinx.features.network.query.redeem_badge_token.NetworkQueryRedeemBadgeTokenImpl
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
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
import chat.sphinx.features.network.query.save_profile.NetworkQuerySaveProfileImpl
import chat.sphinx.features.network.query.subscription.NetworkQuerySubscriptionImpl
import chat.sphinx.features.network.query.verify_external.NetworkQueryAuthorizeExternalImpl
import chat.sphinx.features.network.query.version.NetworkQueryVersionImpl
import chat.sphinx.features.network.relay_call.NetworkRelayCallImpl
import chat.sphinx.features.relay.RelayDataHandlerImpl
import chat.sphinx.features.socket_io.SocketIOManagerImpl
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.build_config.BuildConfigDebug
import chat.sphinx.utils.createTorManager
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.relay.AuthorizationToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.matthewnelson.build_config.BuildConfigVersionCode
import io.matthewnelson.kmp.tor.manager.TorManager
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import okio.FileSystem
import javax.inject.Singleton

/**
 * TODO: Get cache through alternative means...
 */
fun Cache.Companion.networkCache(): Cache {
    return Cache(
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("networkCache").toFile(),
        22
    )
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideTorManager(
        applicationScope: CoroutineScope,
        authenticationStorage: AuthenticationStorage,
        buildConfigDebug: BuildConfigDebug,
        buildConfigVersionCode: BuildConfigVersionCode,
        dispatchers: CoroutineDispatchers,
        LOG: SphinxLogger,
    ): TorManager =
        createTorManager(
            applicationScope,
            authenticationStorage,
            buildConfigDebug,
            buildConfigVersionCode,
            dispatchers,
            LOG
        )

//    @Provides
//    fun provideApplicationServiceTracker(
//        torManagerAndroid: TorManagerAndroid
//    ): ApplicationServiceTracker =
//        torManagerAndroid

    @Provides
    @Singleton
    fun provideRelayDataHandlerImpl(
        authenticationStorage: AuthenticationStorage,
        authenticationCoreManager: AuthenticationCoreManager,
        dispatchers: CoroutineDispatchers,
        encryptionKeyHandler: EncryptionKeyHandler,
        torManager: TorManager,
    ): RelayDataHandlerImpl =
        RelayDataHandlerImpl(
            authenticationStorage,
            authenticationCoreManager,
            dispatchers,
            encryptionKeyHandler,
            torManager
        )

    @Provides
    fun provideRelayDataHandler(
        relayDataHandlerImpl: RelayDataHandlerImpl
    ): RelayDataHandler =
        relayDataHandlerImpl

    @Provides
    @Singleton
    fun provideNetworkClientImpl(
        buildConfigDebug: BuildConfigDebug,
        torManager: TorManager,
        dispatchers: CoroutineDispatchers,
        LOG: SphinxLogger,
    ): NetworkClientImpl =
        NetworkClientImpl(
            buildConfigDebug,
            Cache.networkCache(),
            dispatchers,
            NetworkClientImpl.RedactedLoggingHeaders(
                listOf(
                    AuthorizationToken.AUTHORIZATION_HEADER,
                    AuthenticationToken.HEADER_KEY
                )
            ),
            torManager,
            LOG,
        )

    @Provides
    fun provideNetworkClient(
        networkClientImpl: NetworkClientImpl
    ): NetworkClient =
        networkClientImpl

    @Provides
    fun provideNetworkClientCache(
        networkClientImpl: NetworkClientImpl
    ): NetworkClientCache =
        networkClientImpl

    @Provides
    @Singleton
    fun provideSocketIOManagerImpl(
        dispatchers: CoroutineDispatchers,
        networkClient: NetworkClient,
        relayDataHandler: RelayDataHandler,
        LOG: SphinxLogger,
    ): SocketIOManagerImpl =
        SocketIOManagerImpl(
            dispatchers,
            networkClient,
            relayDataHandler,
            LOG,
        )

    @Provides
    fun provideSocketIOManager(
        socketIOManagerImpl: SocketIOManagerImpl
    ): SocketIOManager =
        socketIOManagerImpl

    @Provides
    @Singleton
    fun provideNetworkRelayCallImpl(
        dispatchers: CoroutineDispatchers,
        networkClient: NetworkClient,
        relayDataHandler: RelayDataHandler,
        sphinxLogger: SphinxLogger,
    ): NetworkRelayCallImpl =
        NetworkRelayCallImpl(
            dispatchers,
            networkClient,
            relayDataHandler,
            sphinxLogger
        )

    @Provides
    fun provideNetworkRelayCall(
        networkRelayCallImpl: NetworkRelayCallImpl
    ): NetworkRelayCall =
        networkRelayCallImpl

    @Provides
    fun provideNetworkCall(
        networkRelayCallImpl: NetworkRelayCallImpl
    ): NetworkCall =
        networkRelayCallImpl

    @Provides
    fun provideLinkPreviewHandler(
        dispatchers: CoroutineDispatchers,
        networkClient: NetworkClient,
        networkQueryChat: NetworkQueryChat,
    ): LinkPreviewHandler =
        LinkPreviewHandlerImpl(dispatchers, networkClient, networkQueryChat)

    @Provides
    @Singleton
    fun provideMemeInputStreamHandler(
        dispatchers: CoroutineDispatchers,
        networkClientCache: NetworkClientCache,
    ): MemeInputStreamHandler =
        MemeInputStreamHandlerImpl(dispatchers, networkClientCache)

    @Provides
    @Singleton
    fun provideNetworkQueryChatImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryChatImpl =
        NetworkQueryChatImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryChat(
        networkQueryChatImpl: NetworkQueryChatImpl
    ): NetworkQueryChat =
        networkQueryChatImpl

    @Provides
    @Singleton
    fun provideNetworkQueryContactImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryContactImpl =
        NetworkQueryContactImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryContact(
        networkQueryContactImpl: NetworkQueryContactImpl
    ): NetworkQueryContact =
        networkQueryContactImpl

    @Provides
    @Singleton
    fun provideNetworkQueryInviteImpl(
        networkRelayCall: NetworkRelayCall,
    ): NetworkQueryInviteImpl =
        NetworkQueryInviteImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryInvite(
        networkQueryInviteImpl: NetworkQueryInviteImpl
    ): NetworkQueryInvite =
        networkQueryInviteImpl

    @Provides
    @Singleton
    fun provideNetworkQueryLightningImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryLightningImpl =
        NetworkQueryLightningImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryLightning(
        networkQueryLightningImpl: NetworkQueryLightningImpl
    ): NetworkQueryLightning =
        networkQueryLightningImpl

    @Provides
    @Singleton
    fun provideNetworkQueryMessageImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryMessageImpl =
        NetworkQueryMessageImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryMessage(
        networkQueryMessageImpl: NetworkQueryMessageImpl
    ): NetworkQueryMessage =
        networkQueryMessageImpl

    @Provides
    @Singleton
    fun provideNetworkQuerySubscriptionImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQuerySubscriptionImpl =
        NetworkQuerySubscriptionImpl(networkRelayCall)

    @Provides
    fun provideNetworkQuerySubscription(
        networkQuerySubscriptionImpl: NetworkQuerySubscriptionImpl
    ): NetworkQuerySubscription =
        networkQuerySubscriptionImpl

    @Provides
    @Singleton
    fun provideNetworkQueryMemeServerImpl(
        dispatchers: CoroutineDispatchers,
        networkRelayCall: NetworkRelayCall,
    ): NetworkQueryMemeServerImpl =
        NetworkQueryMemeServerImpl(dispatchers, networkRelayCall)

    @Provides
    fun provideNetworkQueryMemeServer(
        networkQueryMemeServerImpl: NetworkQueryMemeServerImpl
    ): NetworkQueryMemeServer =
        networkQueryMemeServerImpl

    @Provides
    @Singleton
    fun provideNetworkQueryVersionImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryVersionImpl =
        NetworkQueryVersionImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryVersion(
        networkQueryVersionImpl: NetworkQueryVersionImpl
    ): NetworkQueryVersion =
        networkQueryVersionImpl

    @Provides
    @Singleton
    fun provideNetworkQueryAuthorizeExternalImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryAuthorizeExternalImpl =
        NetworkQueryAuthorizeExternalImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryAuthorizeExternal(
        networkQueryVerifyExternalImpl: NetworkQueryAuthorizeExternalImpl
    ): NetworkQueryAuthorizeExternal =
        networkQueryVerifyExternalImpl

    @Provides
    @Singleton
    fun provideNetworkQueryFeedSearchImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryFeedSearchImpl =
        NetworkQueryFeedSearchImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryFeedSearch(
        networkQueryFeedSearchImpl: NetworkQueryFeedSearchImpl
    ): NetworkQueryFeedSearch =
        networkQueryFeedSearchImpl

    @Provides
    @Singleton
    fun provideNetworkQuerySaveProfileImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQuerySaveProfileImpl =
        NetworkQuerySaveProfileImpl(networkRelayCall)

    @Provides
    fun provideNetworkQuerySaveProfile(
        networkQuerySaveProfileImpl: NetworkQuerySaveProfileImpl
    ): NetworkQuerySaveProfile =
        networkQuerySaveProfileImpl

    @Provides
    @Singleton
    fun provideNetworkQueryRedeemBadgeTokenImpl(
        networkRelayCall: NetworkRelayCall
    ): NetworkQueryRedeemBadgeTokenImpl =
        NetworkQueryRedeemBadgeTokenImpl(networkRelayCall)

    @Provides
    fun provideNetworkQueryRedeemBadgeToken(
        networkQueryRedeemBadgeTokenImpl: NetworkQueryRedeemBadgeTokenImpl
    ): NetworkQueryRedeemBadgeToken =
        networkQueryRedeemBadgeTokenImpl
}
