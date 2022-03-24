package chat.sphinx.di.container

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
    networkModule: NetworkModule
) {
    val accountOwnerFlow: StateFlow<Contact?> = flow {
        emitAll(
            appModule.coreDBImpl.getSphinxDatabaseQueries().contactGetOwner()
                .asFlow()
                .mapToOneOrNull(appModule.dispatchers.io)
                .map { it?.toContact() }
        )
    }.stateIn(
        appModule.applicationScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )
    val memeServerTokenHandlerImpl = MemeServerTokenHandlerImpl(
        accountOwnerFlow,
        appModule.applicationScope,
        authenticationModule.authenticationStorage,
        appModule.dispatchers,
        networkModule.networkQueryMemeServer,
        appModule.sphinxLogger,
    )
    val memeServerTokenHandler = memeServerTokenHandlerImpl
    val sphinxRepositoryPlatform = SphinxRepositoryPlatform(
        accountOwnerFlow,
        appModule.applicationScope,
        authenticationModule.authenticationCoreManager,
        authenticationModule.authenticationStorage,
        appModule.coreDBImpl,
        appModule.dispatchers,
        appModule.mediaCacheHandler,
        networkModule.memeInputStreamHandler,
        memeServerTokenHandler,
        networkModule.networkQueryMemeServer,
        networkModule.networkQueryChat,
        networkModule.networkQueryContact,
        networkModule.networkQueryLightning,
        networkModule.networkQueryMessage,
        networkModule.networkQueryInvite,
        networkModule.networkQueryAuthorizeExternal,
        networkModule.networkQuerySaveProfile,
        networkModule.networkQueryRedeemBadgeToken,
        networkModule.networkQuerySubscription,
        networkModule.networkQueryFeedSearch,
        authenticationModule.rsa,
        networkModule.socketIOManager,
        appModule.sphinxNotificationManager,
        appModule.sphinxLogger,
    )
    val chatRepository = SphinxRepositoryPlatform
    val contactRepository = SphinxRepositoryPlatform
    val lightningRepository = SphinxRepositoryPlatform
    val messageRepository = SphinxRepositoryPlatform
    val subscriptionRepository = SphinxRepositoryPlatform
    val feedRepository = SphinxRepositoryPlatform
    val repositoryMedia = SphinxRepositoryPlatform
}