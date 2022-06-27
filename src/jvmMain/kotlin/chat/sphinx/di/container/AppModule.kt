package chat.sphinx.di.container

import chat.sphinx.concepts.media_cache.MediaCacheHandler
import chat.sphinx.database.SqlDriverUtility
import chat.sphinx.database.SphinxCoreDBImpl
import chat.sphinx.features.coredb.CoreDBImpl
import chat.sphinx.features.media_cache.MediaCacheHandlerImpl
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.SphinxDispatchers
import chat.sphinx.utils.SphinxLoggerImpl
import chat.sphinx.utils.build_config.BuildConfigDebug
import io.matthewnelson.build_config.BuildConfigVersionCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem

class AppModule {
    private val sphinxDispatchers = SphinxDispatchers()
    val dispatchers = sphinxDispatchers
    val applicationScope = CoroutineScope(SupervisorJob() + dispatchers.default)
    private val sphinxLoggerImpl = SphinxLoggerImpl()
    val sphinxLogger: SphinxLogger = sphinxLoggerImpl
    val buildConfigDebug = BuildConfigDebug(true) // TODO: Configure it correctly...
    val buildConfigVersionCode = BuildConfigVersionCode(21) // TODO: Configure it correctly...
    val sqlDriverUtility = SqlDriverUtility()
    val sphinxCoreDBImpl = SphinxCoreDBImpl(
        sqlDriverUtility,
        buildConfigDebug
    )
    val coreDBImpl: CoreDBImpl = sphinxCoreDBImpl
    val mediaCacheHandler: MediaCacheHandler = MediaCacheHandlerImpl(
        applicationScope,
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY,
        dispatchers,
    )
}