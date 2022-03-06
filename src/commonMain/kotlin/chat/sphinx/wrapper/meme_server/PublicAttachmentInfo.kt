package chat.sphinx.wrapper.meme_server

import chat.sphinx.wrapper.message.media.MediaType
import okio.Source

data class PublicAttachmentInfo(
    val source: Source,
    val mediaType: MediaType,
    val fileName: String,
    val contentLength: Long? = null,
)