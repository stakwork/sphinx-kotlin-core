package chat.sphinx.di

import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.media_cache.MediaCacheHandler
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
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
import chat.sphinx.concepts.notification.SphinxNotificationManager
import chat.sphinx.concepts.repository.chat.ChatRepository
import chat.sphinx.concepts.repository.contact.ContactRepository
import chat.sphinx.concepts.repository.feed.FeedRepository
import chat.sphinx.concepts.repository.lightning.LightningRepository
import chat.sphinx.concepts.repository.media.RepositoryMedia
import chat.sphinx.concepts.repository.message.MessageRepository
import chat.sphinx.concepts.repository.subscription.SubscriptionRepository
import chat.sphinx.concepts.socket_io.SocketIOManager
import chat.sphinx.database.DriverFactory
import chat.sphinx.database.SphinxCoreDBImpl
import chat.sphinx.features.coredb.CoreDBImpl
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.features.meme_server.MemeServerTokenHandlerImpl
import chat.sphinx.features.repository.mappers.contact.toContact
import chat.sphinx.features.repository.platform.SphinxRepositoryPlatform
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.build_config.BuildConfigDebug
import chat.sphinx.wrapper.contact.Contact
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDriverFactory(): DriverFactory = DriverFactory()

    @Provides
    @Singleton
    fun provideSphinxCoreDBImpl(
        driverFactory: DriverFactory,
        buildConfigDebug: BuildConfigDebug,
    ): SphinxCoreDBImpl =
        SphinxCoreDBImpl(
            driverFactory,
            buildConfigDebug
        )

    @Provides
    fun provideCoreDBImpl(
        sphinxCoreDBImpl: SphinxCoreDBImpl
    ): CoreDBImpl =
        sphinxCoreDBImpl

    @Provides
    @Singleton
    fun provideAccountOwnerFlow(
        applicationScope: CoroutineScope,
        coreDBImpl: CoreDBImpl,
        dispatchers: CoroutineDispatchers,
    ): StateFlow<Contact?> = flow {
        emitAll(
            coreDBImpl.getSphinxDatabaseQueries().contactGetOwner()
                .asFlow()
                .mapToOneOrNull(dispatchers.io)
                .map { it?.toContact() }
        )
    }.stateIn(
        applicationScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    @Provides
    @Singleton
    fun provideMemeServerTokenHandlerImpl(
        accountOwner: StateFlow<Contact?>,
        applicationScope: CoroutineScope,
        authenticationStorage: AuthenticationStorage,
        dispatchers: CoroutineDispatchers,
        networkQueryMemeServer: NetworkQueryMemeServer,
        LOG: SphinxLogger,
    ): MemeServerTokenHandlerImpl =
        MemeServerTokenHandlerImpl(
            accountOwner,
            applicationScope,
            authenticationStorage,
            dispatchers,
            networkQueryMemeServer,
            LOG,
        )

    @Provides
    fun provideMemeServerTokenHandler(
        memeServerTokenHandlerImpl: MemeServerTokenHandlerImpl
    ): MemeServerTokenHandler =
        memeServerTokenHandlerImpl

    @Provides
    @Singleton
    fun provideSphinxRepositoryPlatform(
        accountOwner: StateFlow<Contact?>,
        applicationScope: CoroutineScope,
        authenticationCoreManager: AuthenticationCoreManager,
        authenticationStorage: AuthenticationStorage,
        coreDBImpl: CoreDBImpl,
        dispatchers: CoroutineDispatchers,
        mediaCacheHandler: MediaCacheHandler,
        memeInputStreamHandler: MemeInputStreamHandler,
        memeServerTokenHandler: MemeServerTokenHandler,
        networkQueryMemeServer: NetworkQueryMemeServer,
        networkQueryChat: NetworkQueryChat,
        networkQueryContact: NetworkQueryContact,
        networkQueryLightning: NetworkQueryLightning,
        networkQueryMessage: NetworkQueryMessage,
        networkQueryInvite: NetworkQueryInvite,
        networkQueryAuthorizeExternal: NetworkQueryAuthorizeExternal,
        networkQuerySaveProfile: NetworkQuerySaveProfile,
        networkQueryRedeemBadgeToken: NetworkQueryRedeemBadgeToken,
        networkQuerySubscription: NetworkQuerySubscription,
        networkQueryFeedSearch: NetworkQueryFeedSearch,
        socketIOManager: SocketIOManager,
        rsa: RSA,
        sphinxNotificationManager: SphinxNotificationManager,
        sphinxLogger: SphinxLogger,
    ): SphinxRepositoryPlatform =
        SphinxRepositoryPlatform(
            accountOwner,
            applicationScope,
            authenticationCoreManager,
            authenticationStorage,
            coreDBImpl,
            dispatchers,
            mediaCacheHandler,
            memeInputStreamHandler,
            memeServerTokenHandler,
            networkQueryMemeServer,
            networkQueryChat,
            networkQueryContact,
            networkQueryLightning,
            networkQueryMessage,
            networkQueryInvite,
            networkQueryAuthorizeExternal,
            networkQuerySaveProfile,
            networkQueryRedeemBadgeToken,
            networkQuerySubscription,
            networkQueryFeedSearch,
            rsa,
            socketIOManager,
            sphinxNotificationManager,
            sphinxLogger,
        )

    @Provides
    fun provideChatRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): ChatRepository =
        SphinxRepositoryPlatform

    @Provides
    fun provideContactRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): ContactRepository =
        SphinxRepositoryPlatform

    @Provides
    fun provideLightningRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): LightningRepository =
        SphinxRepositoryPlatform

    @Provides
    fun provideMessageRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): MessageRepository =
        SphinxRepositoryPlatform

    @Provides
    fun provideSubscriptionRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): SubscriptionRepository =
        SphinxRepositoryPlatform

    @Provides
    fun provideFeedRepository(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): FeedRepository =
        SphinxRepositoryPlatform

//    @Provides
//    @Suppress("UNCHECKED_CAST")
//    fun provideRepositoryDashboardAndroid(
//        SphinxRepositoryPlatform: SphinxRepositoryPlatform
//    ): RepositoryDashboardAndroid<Any> =
//        SphinxRepositoryPlatform as RepositoryDashboardAndroid<Any>

    @Provides
    fun provideRepositoryMedia(
        SphinxRepositoryPlatform: SphinxRepositoryPlatform
    ): RepositoryMedia =
        SphinxRepositoryPlatform
}
