package chat.sphinx.features.repository.model.message

import chat.sphinx.database.core.MessageMediaDbo
import chat.sphinx.utils.platform.getFileSystem
import chat.sphinx.wrapper.message.media.*
import okio.FileSystem
import okio.Path
import kotlin.jvm.Volatile

class MessageMediaDboWrapper(val messageMediaDbo: MessageMediaDbo): MessageMedia() {
    override val mediaKey: MediaKey?
        get() = messageMediaDbo.media_key
    override val mediaType: MediaType
        get() = messageMediaDbo.media_type
    override val mediaToken: MediaToken
        get() = messageMediaDbo.media_token
    override val fileName: FileName?
        get() = messageMediaDbo.file_name

    @Volatile
    @Suppress("PropertyName")
    var _localFile: Path? = messageMediaDbo.local_file

    override val localFile: Path?
        get() = try {
            _localFile?.let { filePath ->
                if (getFileSystem().exists(filePath) && getFileSystem().listOrNull(filePath) == null) {
                    filePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }

    @Volatile
    @Suppress("PropertyName")
    var _mediaKeyDecrypted: MediaKeyDecrypted? = messageMediaDbo.media_key_decrypted
    override val mediaKeyDecrypted: MediaKeyDecrypted?
        get() = _mediaKeyDecrypted

    @Volatile
    @Suppress("PropertyName")
    var _mediaKeyDecryptionError: Boolean = false
    override val mediaKeyDecryptionError: Boolean
        get() = _mediaKeyDecryptionError

    @Volatile
    @Suppress("PropertyName")
    var _mediaKeyDecryptionException: Exception? = null
    override val mediaKeyDecryptionException: Exception?
        get() = _mediaKeyDecryptionException
}