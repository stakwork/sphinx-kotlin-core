package chat.sphinx.wrapper.io_utils

import chat.sphinx.utils.platform.File
import com.stakwork.koi.InputStream


@Suppress("NOTHING_TO_INLINE")
inline fun File.toInputStreamProvider(): InputStreamProvider {
    return object : InputStreamProvider() {
        override fun newInputStream(): InputStream {
            return inputStream()
        }
    }
}

abstract class InputStreamProvider {
    abstract fun newInputStream(): InputStream
}
