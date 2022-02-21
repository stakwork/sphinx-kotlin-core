/*
 * Copyright (c) 1994, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.stakwork.koi

import chat.sphinx.utils.checkFromIndexSize
import kotlinx.io.errors.IOException
import kotlin.jvm.JvmField
import kotlin.jvm.Synchronized
import kotlin.math.min

/**
 * A `ByteArrayInputStream` contains
 * an internal buffer that contains bytes that
 * may be read from the stream. An internal
 * counter keeps track of the next byte to
 * be supplied by the `read` method.
 *
 *
 * Closing a `ByteArrayInputStream` has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an `IOException`.
 *
 * @author  Arthur van Hoff
 * @see StringBufferInputStream
 *
 * @since   1.0
 */
class ByteArrayInputStream : InputStream {
    /**
     * An array of bytes that was provided
     * by the creator of the stream. Elements `buf[0]`
     * through `buf[count-1]` are the
     * only bytes that can ever be read from the
     * stream;  element `buf[pos]` is
     * the next byte to be read.
     */
    protected var buf: ByteArray

    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative
     * and not larger than the value of `count`.
     * The next byte to be read from the input stream buffer
     * will be `buf[pos]`.
     */
    protected var pos: Int

    /**
     * The currently marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by the `mark()` method.
     * The current buffer position is set to this point by the
     * `reset()` method.
     *
     *
     * If no mark has been set, then the value of mark is the offset
     * passed to the constructor (or 0 if the offset was not supplied).
     *
     * @since   1.1
     */
    protected var mark = 0

    /**
     * The index one greater than the last valid character in the input
     * stream buffer.
     * This value should always be nonnegative
     * and not larger than the length of `buf`.
     * It  is one greater than the position of
     * the last byte within `buf` that
     * can ever be read  from the input stream buffer.
     */
    @JvmField
    var count: Int

    /**
     * Creates a `ByteArrayInputStream`
     * so that it  uses `buf` as its
     * buffer array.
     * The buffer array is not copied.
     * The initial value of `pos`
     * is `0` and the initial value
     * of  `count` is the length of
     * `buf`.
     *
     * @param   buf   the input buffer.
     */
    constructor(buf: ByteArray) {
        this.buf = buf
        pos = 0
        count = buf.size
    }

    /**
     * Creates `ByteArrayInputStream`
     * that uses `buf` as its
     * buffer array. The initial value of `pos`
     * is `offset` and the initial value
     * of `count` is the minimum of `offset+length`
     * and `buf.length`.
     * The buffer array is not copied. The buffer's mark is
     * set to the specified offset.
     *
     * @param   buf      the input buffer.
     * @param   offset   the offset in the buffer of the first byte to read.
     * @param   length   the maximum number of bytes to read from the buffer.
     */
    constructor(buf: ByteArray, offset: Int, length: Int) {
        this.buf = buf
        pos = offset
        count = min(offset + length, buf.size)
        mark = offset
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an `int` in the range
     * `0` to `255`. If no byte is available
     * because the end of the stream has been reached, the value
     * `-1` is returned.
     *
     *
     * This `read` method
     * cannot block.
     *
     * @return  the next byte of data, or `-1` if the end of the
     * stream has been reached.
     */
    @Synchronized
    override fun read(): Int {
        return if (pos < count) buf[pos++].toInt() and 0xff else -1
    }

    /**
     * Reads up to `len` bytes of data into an array of bytes from this
     * input stream.  If `pos` equals `count`, then `-1` is
     * returned to indicate end of file.  Otherwise, the  number `k` of
     * bytes read is equal to the smaller of `len` and `count-pos`.
     * If `k` is positive, then bytes `buf[pos]` through
     * `buf[pos+k-1]` are copied into `b[off]` through
     * `b[off+k-1]` in the manner performed by `System.arraycopy`.
     * The value `k` is added into `pos` and `k` is returned.
     *
     *
     * Unlike the [overridden method][InputStream.read]
     * of `InputStream`, this method returns `-1` instead of zero
     * if the end of the stream has been reached and `len == 0`.
     *
     *
     * This `read` method cannot block.
     *
     * @param   b     the buffer into which the data is read.
     * @param   off   the start offset in the destination array `b`
     * @param   len   the maximum number of bytes read.
     * @return  the total number of bytes read into the buffer, or
     * `-1` if there is no more data because the end of
     * the stream has been reached.
     * @throws  NullPointerException If `b` is `null`.
     * @throws  IndexOutOfBoundsException If `off` is negative,
     * `len` is negative, or `len` is greater than
     * `b.length - off`
     */
    @Synchronized
    override fun read(b: ByteArray, off: Int, length: Int): Int {
        var len = length
        checkFromIndexSize(off, len, b.size)
        if (pos >= count) {
            return -1
        }
        val avail = count - pos
        if (len > avail) {
            len = avail
        }
        if (len <= 0) {
            return 0
        }
        buf.copyInto(b, off, pos, len)
        pos += len
        return len
    }

    @Synchronized
    override fun readAllBytes(): ByteArray {
        val result = buf.copyOfRange(pos, count)
        pos = count
        return result
    }

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        val n = read(b, off, len)
        return if (n == -1) 0 else n
    }

    @Synchronized
    @Throws(IOException::class)
    override fun transferTo(out: OutputStream): Long {
        val len = count - pos
        out.write(buf, pos, len)
        pos = count
        return len.toLong()
    }

    /**
     * Skips `n` bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number `k`
     * of bytes to be skipped is equal to the smaller
     * of `n` and  `count-pos`.
     * The value `k` is added into `pos`
     * and `k` is returned.
     *
     * @param   n   the number of bytes to be skipped.
     * @return  the actual number of bytes skipped.
     */
    @Synchronized
    override fun skip(n: Long): Long {
        var k = (count - pos).toLong()
        if (n < k) {
            k = if (n < 0) 0 else n
        }
        pos += k.toInt()
        return k
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over)
     * from this input stream.
     *
     *
     * The value returned is `count - pos`,
     * which is the number of bytes remaining to be read from the input buffer.
     *
     * @return  the number of remaining bytes that can be read (or skipped
     * over) from this input stream without blocking.
     */
    @Synchronized
    override fun available(): Int {
        return count - pos
    }

    /**
     * Tests if this `InputStream` supports mark/reset. The
     * `markSupported` method of `ByteArrayInputStream`
     * always returns `true`.
     *
     * @since   1.1
     */
    override fun markSupported(): Boolean {
        return true
    }

    /**
     * Set the current marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by this method.
     *
     *
     * If no mark has been set, then the value of the mark is the
     * offset passed to the constructor (or 0 if the offset was not
     * supplied).
     *
     *
     *  Note: The `readAheadLimit` for this class
     * has no meaning.
     *
     * @since   1.1
     */
    override fun mark(readAheadLimit: Int) {
        mark = pos
    }

    /**
     * Resets the buffer to the marked position.  The marked position
     * is 0 unless another position was marked or an offset was specified
     * in the constructor.
     */
    @Synchronized
    override fun reset() {
        pos = mark
    }

    /**
     * Closing a `ByteArrayInputStream` has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an `IOException`.
     */
    @Throws(IOException::class)
    override fun close() {
    }
}