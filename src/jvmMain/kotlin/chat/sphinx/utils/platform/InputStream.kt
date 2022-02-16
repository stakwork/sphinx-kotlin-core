package chat.sphinx.utils.platform

actual abstract class InputStream {
    protected lateinit var inputStream: java.io.InputStream

    actual open fun read(buffer: ByteArray): Int {
        return inputStream.read(buffer)
    }

    actual open fun close() {
        inputStream.close()
    }
}