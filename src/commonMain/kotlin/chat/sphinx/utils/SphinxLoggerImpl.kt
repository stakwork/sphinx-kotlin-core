package chat.sphinx.utils

import chat.sphinx.logger.LogType
import chat.sphinx.logger.SphinxLogger

/**
 * TODO: Implement an actual multiplatform logger...
 */
class SphinxLoggerImpl: SphinxLogger() {
    override fun log(tag: String, message: String, type: LogType, throwable: Throwable?) {
        val nnThrowable = throwable ?: ""
        println("$tag: $message $nnThrowable")
    }
}