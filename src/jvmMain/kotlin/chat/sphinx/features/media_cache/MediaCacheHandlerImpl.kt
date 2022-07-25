package chat.sphinx.features.media_cache

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.media_cache.MediaCacheHandler
import chat.sphinx.wrapper.message.media.MediaType
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.io.errors.IOException
import okio.*
import java.io.File
import java.io.InputStream
import kotlin.io.use

class MediaCacheHandlerImpl(
    private val applicationScope: CoroutineScope,
    cacheDir: Path,
    dispatchers: CoroutineDispatchers,
): MediaCacheHandler(), CoroutineDispatchers by dispatchers {

    init {
        if (!FileSystem.SYSTEM.exists(cacheDir)) {
            FileSystem.SYSTEM.createDirectories(cacheDir)
        } else {
            require(cacheDir.toFile().isDirectory) {
                "cacheDir must be a directory"
            }
        }
    }

    companion object {
        const val AUDIO_CACHE_DIR = "sphinx_audio_cache"
        const val IMAGE_CACHE_DIR = "sphinx_image_cache"
        const val VIDEO_CACHE_DIR = "sphinx_video_cache"
        const val PDF_CACHE_DIR = "sphinx_pdf_cache"
        const val PAID_TEXT_CACHE_DIR = "sphinx_paid_text_cache"
        const val GENERIC_FILES_CACHE_DIR = "sphinx_files_cache"

        const val DATE_FORMAT = "yyy_MM_dd_HH_mm_ss_SSS"

        const val AUD = "AUD"
        const val IMG = "IMG"
        const val VID = "VID"
        const val PDF = "PDF"
        const val TXT = "TXT"
        const val FILE = "FILE"

        private val cacheDirLock = SynchronizedObject()
    }

    private val audioCache: Path by lazy {
        cacheDir.resolve(AUDIO_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    private val imageCache: Path by lazy {
        cacheDir.resolve(IMAGE_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    private val videoCache: Path by lazy {
        cacheDir.resolve(VIDEO_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    private val pdfCache: Path by lazy {
        cacheDir.resolve(PDF_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    private val genericFilesCache: Path by lazy {
        cacheDir.resolve(GENERIC_FILES_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    private val paidTextCache: Path by lazy {
        cacheDir.resolve(PAID_TEXT_CACHE_DIR).also {
            FileSystem.SYSTEM.createDirectory(it)
        }
    }

    override fun createFile(
        mediaType: MediaType,
        extension: String?
    ): Path? {
        return when (mediaType) {
            is MediaType.Audio -> {
                mediaType.value.split("/").lastOrNull()?.let { fileType ->
                    when {
                        fileType.contains("m4a", ignoreCase = true) -> {
                            createAudioFile("m4a")
                        }
                        fileType.contains("mp3", ignoreCase = true) -> {
                            createAudioFile("mp3")
                        }
                        fileType.contains("mp4", ignoreCase = true) -> {
                            createAudioFile("mp4")
                        }
                        fileType.contains("mpeg", ignoreCase = true) -> {
                            createAudioFile("mpeg")
                        }
                        fileType.contains("wav", ignoreCase = true) -> {
                            createAudioFile("wav")
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
            is MediaType.Image -> {
                // use image loader
                null
            }
            is MediaType.Pdf -> {
                mediaType.value.split("/").lastOrNull()?.let { fileType ->
                    when {
                        fileType.contains("pdf", ignoreCase = true) -> {
                            createPdfFile("pdf")
                        }
                        else -> {
                            null
                        }
                    }
                }

            }
            is MediaType.Text -> {
                // TODO: Implement
                null
            }
            is MediaType.Video -> {
                // TODO: Auto generate file extension (if app doesn't support media we can load via )
                mediaType.value.split("/").lastOrNull()?.let { fileType ->
                    when {
                        fileType.contains("webm", ignoreCase = true) -> {
                            createVideoFile("webm")
                        }
                        fileType.contains("3gpp", ignoreCase = true) -> {
                            createVideoFile("3gp")
                        }
                        fileType.contains("x-matroska", ignoreCase = true) -> {
                            createVideoFile("mkv")
                        }
                        fileType.contains("mp4", ignoreCase = true) -> {
                            createVideoFile("mp4")
                        }
                        fileType.contains("mov", ignoreCase = true) -> {
                            createVideoFile("mov")
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
            is MediaType.Unknown -> {
                createGenericFile(extension ?: "txt")
            }
        }
    }

    override fun createAudioFile(extension: String): Path =
        createFileImpl(audioCache, AUD, extension)

    override fun createImageFile(extension: String): Path =
        createFileImpl(imageCache, IMG, extension)

    override fun createVideoFile(extension: String): Path =
        createFileImpl(videoCache, VID, extension)

    override fun createPdfFile(extension: String): Path =
        createFileImpl(pdfCache, PDF, extension)

    override fun createPaidTextFile(extension: String): Path =
        createFileImpl(paidTextCache, TXT, extension)

    private fun createGenericFile(extension: String): Path =
        createFileImpl(genericFilesCache, FILE, extension)

    private fun createFileImpl(cacheDir: Path, prefix: String, extension: String): Path {
        if (!FileSystem.SYSTEM.exists(cacheDir)) {
            synchronized(cacheDirLock) {
                if (!FileSystem.SYSTEM.exists(cacheDir)) {
                    FileSystem.SYSTEM.createDirectories(cacheDir)
                }
            }
        }

        val ext = extension.replace(".", "")
        val sdf = DateFormat(DATE_FORMAT)
        return cacheDir.resolve("${prefix}_${sdf.format(DateTimeTz.nowLocal())}.$ext")
    }

    // TODO: Implement file deletion on caller scope cancellation
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun copyTo(from: Path, to: Path): Path {
        copyToImpl(from, to).join()
        return to
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun copyTo(from: InputStream, to: Path): Path {
        copyToImpl(from.source(), FileSystem.SYSTEM.sink(to).buffer()).join()
        return to
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun copyToImpl(from: Source, to: BufferedSink): Job {
        return applicationScope.launch(io) {
            from.use {
                try {
                    to.writeAll(it)
                } catch (e: IOException) {
                } finally {

                    try {
                        to.close()
                    } catch (e: IOException) {}

                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun copyToImpl(from: Path, to: Path): Job {
        return applicationScope.launch(io) {
            FileSystem.SYSTEM.copy(from, to)
        }
    }
}