package chat.sphinx.utils

import kotlinx.io.errors.IOException

/**
 * Kotlin Multiplatform rewrite for java.io.Flushable
 */
interface Flushable {
    @Throws(IOException::class)
    fun flush()
}