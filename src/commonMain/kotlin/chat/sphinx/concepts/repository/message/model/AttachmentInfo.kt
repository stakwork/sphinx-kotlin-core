package chat.sphinx.concepts.repository.message.model

import chat.sphinx.wrapper.message.media.FileName
import chat.sphinx.wrapper.message.media.MediaType
import okio.Path

data class AttachmentInfo(
    val filePath: Path,
    val mediaType: MediaType,
    val fileName: FileName?,
    val isLocalFile: Boolean,
)
