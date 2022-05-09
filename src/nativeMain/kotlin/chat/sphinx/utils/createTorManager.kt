package chat.sphinx.utils

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.build_config.BuildConfigDebug
import io.matthewnelson.build_config.BuildConfigVersionCode
import kotlinx.coroutines.CoroutineScope

//actual fun createTorManager(
//    applicationScope: CoroutineScope,
//    authenticationStorage: AuthenticationStorage,
//    buildConfigDebug: BuildConfigDebug,
//    buildConfigVersionCode: BuildConfigVersionCode,
//    dispatchers: CoroutineDispatchers,
//    LOG: SphinxLogger
//): TorManager {
//    TODO("Not yet implemented")
//}