package chat.sphinx.utils

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.io.core.Closeable
import kotlinx.io.errors.IOException
import kotlin.jvm.Volatile

/**
 * Kotlin Multiplatform rewrite for java.io.Writer
 */
abstract class Writer() : Appendable, Closeable, Flushable {
    private var writeBuffer: CharArray? = null
    protected var lock = SynchronizedObject()

    protected constructor(lock: SynchronizedObject?) : this() {
        if (lock == null) {
            throw NullPointerException()
        } else {
            this.lock = lock
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    @Throws(IOException::class)
    open fun write(c: Int) {
        synchronized(lock) {
            if (writeBuffer == null) {
                writeBuffer = CharArray(1024)
            }
            writeBuffer!![0] = c.toChar()
            this.write(writeBuffer!!, 0, 1)
        }
    }

    @Throws(IOException::class)
    fun write(cbuf: CharArray) {
        this.write(cbuf, 0, cbuf.size)
    }

    @Throws(IOException::class)
    abstract fun write(cbuf: CharArray, off: Int, len: Int)

    @Throws(IOException::class)
    open fun write(str: String) {
        this.write(str, 0, str.length)
    }

    @Throws(IOException::class)
    open fun write(str: String, off: Int, len: Int) {
        synchronized(lock!!) {
            val cbuf = if (len <= 1024) {
                if (writeBuffer == null) {
                    writeBuffer = CharArray(1024)
                }
                writeBuffer!!
            } else {
                CharArray(len)
            }

            // Kotlin multiplatform version of
            // str.getChars(off, off + len, cbuf, 0);
            str.toCharArray().copyInto(
                destination = cbuf,
                destinationOffset = 0,
                startIndex = off,
                endIndex = off + len
            )
            this.write(cbuf, 0, len)
        }
    }

    override fun append(value: CharSequence?): Writer {
        this.write(value.toString())
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Writer {
        var csq: CharSequence? = value
        if (csq == null) {
            csq = "null"
        }
        return this.append(csq.subSequence(startIndex, endIndex))
    }

    override fun append(value: Char): Writer {
        this.write(value.toInt())
        return this
    }

    @Throws(IOException::class)
    abstract override fun flush()

    abstract override fun close()

    companion object {
        private const val WRITE_BUFFER_SIZE = 1024
        fun nullWriter(): Writer {
            return object : Writer() {
                @Volatile
                private var closed = false
                @Throws(IOException::class)
                private fun ensureOpen() {
                    if (closed) {
                        throw IOException("Stream closed")
                    }
                }

                override fun append(c: Char): Writer {
                    ensureOpen()
                    return this
                }

                override fun append(value: CharSequence?): Writer {
                    ensureOpen()
                    return this
                }

                override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Writer {
                    ensureOpen()
                    if (value != null) {
                        checkFromToIndex(startIndex, endIndex, value.length)
                    }
                    return this
                }

                @Throws(IOException::class)
                override fun write(c: Int) {
                    ensureOpen()
                }

                @Throws(IOException::class)
                override fun write(cbuf: CharArray, off: Int, len: Int) {
                    checkFromIndexSize(off, len, cbuf.size)
                    ensureOpen()
                }

                @Throws(IOException::class)
                override fun write(str: String) {
                    ensureOpen()
                }

                @Throws(IOException::class)
                override fun write(str: String, off: Int, len: Int) {
                    checkFromIndexSize(off, len, str.length)
                    ensureOpen()
                }

                @Throws(IOException::class)
                override fun flush() {
                    ensureOpen()
                }

                override fun close() {
                    closed = true
                }
            }
        }
    }
}