package chat.sphinx.authentication

import chat.sphinx.logger.SphinxLogger
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

@OptIn(ExperimentalSettingsImplementation::class)
actual fun createSettings(): Settings {
    val delegate: Preferences = Preferences.userNodeForPackage(SphinxLogger::class.java)
    return JvmPreferencesSettings(delegate)
}

