package chat.sphinx.utils.platform

actual fun getPlatformName(): String {
    return "JVM"
}

actual fun getCurrentTimeInMillis() = System.currentTimeMillis()