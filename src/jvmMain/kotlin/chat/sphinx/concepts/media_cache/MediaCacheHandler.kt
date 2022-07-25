package chat.sphinx.concepts.media_cache

import chat.sphinx.wrapper.message.media.MediaType
import okio.Path
import java.io.InputStream

abstract class MediaCacheHandler {
    abstract fun createFile(mediaType: MediaType, extension: String? = null): Path?
    abstract fun createAudioFile(extension: String): Path
    abstract fun createImageFile(extension: String): Path
    abstract fun createVideoFile(extension: String): Path
    abstract fun createPdfFile(extension: String) : Path
    abstract fun createPaidTextFile(extension: String): Path

    abstract suspend fun copyTo(from: Path, to: Path): Path
    abstract suspend fun copyTo(from: InputStream, to: Path): Path
}
