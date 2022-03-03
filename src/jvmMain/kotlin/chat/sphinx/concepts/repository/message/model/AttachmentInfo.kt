package chat.sphinx.concepts.repository.message.model

import chat.sphinx.utils.platform.File
import chat.sphinx.wrapper.message.media.MediaType

data class AttachmentInfo(
    val file: File,
    val mediaType: MediaType,
    val isLocalFile: Boolean,
)
