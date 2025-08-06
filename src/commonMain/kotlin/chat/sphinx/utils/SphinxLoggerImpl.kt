package chat.sphinx.utils

import chat.sphinx.logger.LogType
import chat.sphinx.logger.SphinxLogger

/**
 * TODO: Implement an actual multiplatform logger...
 */
class SphinxLoggerImpl : SphinxLogger() {

    companion object {
        private const val ENABLE_LOGS = false
    }

    override fun log(tag: String, message: String, type: LogType, throwable: Throwable?) {
        if (!ENABLE_LOGS) return

        val nnThrowable = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        println("$tag [$type]: $message $nnThrowable")
    }
}
