package chat.sphinx.utils.platform

import kotlin.system.getTimeMillis

actual fun getPlatformName(): String {
    return "native"
}

actual fun getCurrentTimeInMillis() = getTimeMillis()