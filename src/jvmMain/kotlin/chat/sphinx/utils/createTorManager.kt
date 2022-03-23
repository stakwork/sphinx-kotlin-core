package chat.sphinx.utils

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.build_config.BuildConfigDebug
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.Settings
import io.matthewnelson.build_config.BuildConfigVersionCode
import io.matthewnelson.kmp.tor.manager.TorManager
import kotlinx.coroutines.CoroutineScope
import java.util.prefs.Preferences

actual fun createTorManager(
    applicationScope: CoroutineScope,
    authenticationStorage: AuthenticationStorage,
    buildConfigDebug: BuildConfigDebug,
    buildConfigVersionCode: BuildConfigVersionCode,
    dispatchers: CoroutineDispatchers,
    LOG: SphinxLogger
): TorManager {
    TODO("Not yet implemented")
}

@OptIn(ExperimentalSettingsImplementation::class)
actual fun createPlatformSettings(): Settings {
    val preferences = Preferences.userRoot()
    return JvmPreferencesSettings(preferences)
}