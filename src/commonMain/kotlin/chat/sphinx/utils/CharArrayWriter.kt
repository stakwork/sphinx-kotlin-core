package chat.sphinx.utils

import kotlinx.atomicfu.locks.synchronized
import kotlinx.io.errors.IOException
import kotlin.jvm.JvmOverloads
import kotlin.math.max

/**
 * Kotlin Multiplatform rewrite for java.io.CharArrayWriter
 */
open class CharArrayWriter @JvmOverloads constructor(initialSize: Int = 32) : Writer() {
    protected var buf: CharArray
    protected var count = 0

    override fun write(c: Int) {
        synchronized(lock) {
            val newcount = count + 1
            if (newcount > buf.size) {
                buf = buf.copyOf(max(buf.size shl 1, newcount))
            }
            buf[count] = c.toChar()
            count = newcount
        }
    }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        if (off >= 0 && off <= cbuf.size && len >= 0 && off + len <= cbuf.size && off + len >= 0) {
            if (len != 0) {
                synchronized(lock) {
                    val newcount = count + len
                    if (newcount > buf.size) {
                        buf = buf.copyOf(max(buf.size shl 1, newcount))
                    }
                    cbuf.copyInto(buf, count, off, len)
//                    System.arraycopy(c, off, buf, count, len)
                    count = newcount
                }
            }
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    override fun write(str: String, off: Int, len: Int) {
        synchronized(lock) {
            val newcount = count + len
            if (newcount > buf.size) {
                buf = buf.copyOf(max(buf.size shl 1, newcount))
            }

            // Kotlin Multiplatform for
            // str.getChars(off, off + len, this.buf, this.count);
            str.toCharArray().copyInto(
                destination = buf,
                destinationOffset = count,
                startIndex = off,
                endIndex = off + len
            )
            count = newcount
        }
    }

    @Throws(IOException::class)
    fun writeTo(out: Writer) {
        synchronized(lock) { out.write(buf, 0, count) }
    }

    override fun append(value: CharSequence?): CharArrayWriter {
        val s = value.toString()
        this.write(s, 0, s.length)
        return this
    }

    override fun append(csq: CharSequence?, startIndex: Int, endIndex: Int): CharArrayWriter {
        var csq: CharSequence? = csq
        if (csq == null) {
            csq = "null"
        }
        return this.append(csq.subSequence(startIndex, endIndex))
    }

    override fun append(value: Char): CharArrayWriter {
        this.write(value.code)
        return this
    }

    fun reset() {
        count = 0
    }

    fun toCharArray(): CharArray {
        synchronized(lock) { return buf.copyOf(count) }
    }

    fun size(): Int {
        return count
    }

    override fun toString(): String {
        synchronized(lock) { return buf.concatToString(0, count) }
    }

    override fun flush() {}
    override fun close() {}

    init {
        require(initialSize >= 0) { "Negative initial size: $initialSize" }
        buf = CharArray(initialSize)
    }
}