package chat.sphinx.di.container

object SphinxContainer {
    val appModule = AppModule()
    val authenticationModule = AuthenticationModule(
        appModule
    )
    val networkModule = NetworkModule(
        appModule,
        authenticationModule
    )
    val repositoryModule = RepositoryModule(
        appModule,
        authenticationModule,
        networkModule
    )




}