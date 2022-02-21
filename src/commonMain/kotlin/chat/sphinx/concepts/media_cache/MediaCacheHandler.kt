package chat.sphinx.concepts.media_cache

import chat.sphinx.utils.platform.File
import chat.sphinx.wrapper.message.media.MediaType
import com.stakwork.koi.InputStream

abstract class MediaCacheHandler {
    abstract fun createFile(mediaType: MediaType): File?
    abstract fun createAudioFile(extension: String): File
    abstract fun createImageFile(extension: String): File
    abstract fun createVideoFile(extension: String): File
    abstract fun createPaidTextFile(extension: String): File

    abstract suspend fun copyTo(from: File, to: File): File
    abstract suspend fun copyTo(from: InputStream, to: File): File
}
