package chat.sphinx.utils.platform

import okio.FileSystem

actual fun getFileSystem(): FileSystem = FileSystem.SYSTEM