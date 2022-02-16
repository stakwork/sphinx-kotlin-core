package chat.sphinx.wrapper.meme_server

import chat.sphinx.wrapper.io_utils.InputStreamProvider


data class PublicAttachmentInfo(
    val stream: InputStreamProvider,
    val mediaType: MediaType,
    val fileName: String,
    val contentLength: Long? = null,
)