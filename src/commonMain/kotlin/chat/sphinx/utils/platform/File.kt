package chat.sphinx.utils.platform

expect class File(path: String) {

    constructor(parent: File, child: String)

    fun exists(): Boolean

    fun delete(): Boolean

    fun getAbsolutePath(): String

    fun isFile(): Boolean

    fun isDirectory(): Boolean

    fun mkdir(): Boolean

    fun mkdirs(): Boolean
}