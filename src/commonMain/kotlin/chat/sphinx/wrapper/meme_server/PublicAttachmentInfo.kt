package chat.sphinx.wrapper.meme_server

import chat.sphinx.wrapper.message.media.MediaType
import okio.Path

data class PublicAttachmentInfo(
    val path: Path,
    val mediaType: MediaType,
    val fileName: String,
    val contentLength: Long? = null,
)