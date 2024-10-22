package chat.sphinx.di.container

import chat.sphinx.concepts.authentication.coordinator.AuthenticationCoordinator
import chat.sphinx.concepts.authentication.core.AuthenticationManager
import chat.sphinx.concepts.notification.SphinxNotificationManager

object SphinxContainer {
    val appModule = AppModule()
    val authenticationModule = AuthenticationModule(
        appModule
    )
    val networkModule = NetworkModule(
        appModule,
        authenticationModule
    )
    fun repositoryModule(
        sphinxNotificationManager: SphinxNotificationManager
    ) = RepositoryModule(
        appModule,
        authenticationModule,
        networkModule,
        sphinxNotificationManager
    )
}