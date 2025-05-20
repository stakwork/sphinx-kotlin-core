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

    private var repositoryModuleInstance: RepositoryModule? = null

    fun repositoryModule(
        sphinxNotificationManager: SphinxNotificationManager
    ): RepositoryModule {
        if (repositoryModuleInstance == null) {
            repositoryModuleInstance = RepositoryModule(
                appModule,
                authenticationModule,
                networkModule,
                sphinxNotificationManager
            )
        }
        return repositoryModuleInstance!!
    }
}