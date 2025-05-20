package chat.sphinx.di.container

import chat.sphinx.concepts.connect_manager.ConnectManager
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.notification.SphinxNotificationManager
import chat.sphinx.concepts.repository.chat.ChatRepository
import chat.sphinx.concepts.repository.connect_manager.ConnectManagerRepository
import chat.sphinx.concepts.repository.contact.ContactRepository
import chat.sphinx.concepts.repository.dashboard.RepositoryDashboard
import chat.sphinx.concepts.repository.feed.FeedRepository
import chat.sphinx.concepts.repository.lightning.LightningRepository
import chat.sphinx.concepts.repository.media.RepositoryMedia
import chat.sphinx.concepts.repository.message.MessageRepository
import chat.sphinx.concepts.repository.subscription.SubscriptionRepository
import chat.sphinx.features.meme_server.MemeServerTokenHandlerImpl
import chat.sphinx.features.repository.mappers.contact.toContact
import chat.sphinx.features.repository.platform.SphinxRepositoryPlatform
import chat.sphinx.wrapper.contact.Contact
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.*

class RepositoryModule(
    appModule: AppModule,
    authenticationModule: AuthenticationModule,
    networkModule: NetworkModule,
    sphinxNotificationManager: SphinxNotificationManager
) {
    private val accountOwnerFlow: StateFlow<Contact?> = flow {
        emitAll(
            appModule.coreDBImpl.getSphinxDatabaseQueries().contactGetOwner()
                .asFlow()
                .mapToOneOrNull(appModule.dispatchers.io)
                .map {
                    it?.toContact()
                }
        )
    }.stateIn(
        appModule.applicationScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    private val memeServerTokenHandlerImpl = MemeServerTokenHandlerImpl(
        accountOwnerFlow,
        appModule.applicationScope,
        authenticationModule.authenticationStorage,
        appModule.dispatchers,
        networkModule.networkQueryMemeServer,
        appModule.sphinxLogger,
    )

    val memeServerTokenHandler: MemeServerTokenHandler = memeServerTokenHandlerImpl

    private val sphinxRepositoryPlatform = SphinxRepositoryPlatform(
        accountOwnerFlow,
        appModule.applicationScope,
        authenticationModule.authenticationCoreManager,
        authenticationModule.authenticationStorage,
        networkModule.relayDataHandler,
        appModule.coreDBImpl,
        appModule.dispatchers,
        appModule.mediaCacheHandler,
        networkModule.memeInputStreamHandler,
        memeServerTokenHandler,
        networkModule.networkQueryMemeServer,
        networkModule.networkQueryChat,
        networkModule.networkQueryContact,
        networkModule.networkQueryAuthorizeExternal,
        networkModule.networkQuerySaveProfile,
        networkModule.networkQueryRedeemBadgeToken,
        networkModule.networkQueryFeedSearch,
        appModule.connectManager,
        authenticationModule.rsa,
        sphinxNotificationManager,
        appModule.sphinxLogger,
    )

    val chatRepository: ChatRepository = sphinxRepositoryPlatform
    val contactRepository: ContactRepository = sphinxRepositoryPlatform
    val lightningRepository: LightningRepository = sphinxRepositoryPlatform
    val messageRepository: MessageRepository = sphinxRepositoryPlatform
    val subscriptionRepository: SubscriptionRepository = sphinxRepositoryPlatform
    val feedRepository: FeedRepository = sphinxRepositoryPlatform
    val connectManagerRepository: ConnectManagerRepository = sphinxRepositoryPlatform
    val networkQueryContact: NetworkQueryContact = networkModule.networkQueryContact

    val repositoryDashboard: RepositoryDashboard = sphinxRepositoryPlatform
    val repositoryMedia: RepositoryMedia = sphinxRepositoryPlatform
}