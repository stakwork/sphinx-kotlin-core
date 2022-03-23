package chat.sphinx.di

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.notification.SphinxNotificationManager
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.SphinxDispatchers
import chat.sphinx.utils.SphinxLoggerImpl
import chat.sphinx.utils.build_config.BuildConfigDebug
import io.matthewnelson.build_config.BuildConfigVersionCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSphinxDispatchers(): SphinxDispatchers =
        SphinxDispatchers()

    @Provides
    fun provideCoroutineDispatchers(
        sphinxDispatchers: SphinxDispatchers
    ): CoroutineDispatchers =
        sphinxDispatchers

    @Provides
    @Singleton
    fun provideApplicationScope(
        dispatchers: CoroutineDispatchers
    ): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.default)

    @Provides
    @Singleton
    fun provideSphinxLoggerImpl(
        buildConfigDebug: BuildConfigDebug,
    ): SphinxLoggerImpl =
        SphinxLoggerImpl()

    @Provides
    fun provideSphinxLogger(
        sphinxLoggerImpl: SphinxLoggerImpl
    ): SphinxLogger =
        sphinxLoggerImpl

    @Provides
    @Singleton
    fun provideBuildConfigDebug(): BuildConfigDebug =
        BuildConfigDebug(true) // TODO: Figure out if we are a debug build...
//        BuildConfigDebug(BuildConfig.DEBUG)

    @Provides
    @Singleton
    fun provideBuildConfigVersionCode(): BuildConfigVersionCode =
//        BuildConfigVersionCode(BuildConfig.VERSION_CODE)
        BuildConfigVersionCode(21)

//    @Provides
//    @Singleton
//    fun provideImageLoaderAndroid(
//        @ApplicationContext appContext: Context,
//        dispatchers: CoroutineDispatchers,
//        networkClientCache: NetworkClientCache,
//        LOG: SphinxLogger,
//    ): ImageLoaderAndroid =
//        ImageLoaderAndroid(appContext, dispatchers, networkClientCache, LOG)

//    @Provides
//    fun provideImageLoader(
//        imageLoaderAndroid: ImageLoaderAndroid
//    ): ImageLoader<ImageView> =
//        imageLoaderAndroid

//    @Provides
//    @Singleton
//    fun provideMediaCacheHandler(
//        applicationScope: CoroutineScope,
//        application: Application,
//        dispatchers: CoroutineDispatchers,
//    ): MediaCacheHandler =
//        MediaCacheHandlerImpl(
//            applicationScope,
//            application.cacheDir,
//            dispatchers,
//        )
//
//    @Provides
//    @Singleton
//    fun provideSphinxNotificationManager(
//        @ApplicationContext appContext: Context,
//        sphinxLogger: SphinxLogger,
//    ): SphinxNotificationManager = SphinxNotificationManagerImpl(
//        appContext,
//        sphinxLogger
//    )
}
