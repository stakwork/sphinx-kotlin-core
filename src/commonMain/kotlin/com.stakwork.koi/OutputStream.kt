/*
 * Copyright (c) 1994, 2019, Oracle and/or its affiliates. All rights reserved.
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
import kotlin.jvm.Volatile

/**
 * This abstract class is the superclass of all classes representing
 * an output stream of bytes. An output stream accepts output bytes
 * and sends them to some sink.
 *
 *
 * Applications that need to define a subclass of
 * `OutputStream` must always provide at least a method
 * that writes one byte of output.
 *
 * @author  Arthur van Hoff
 * @see BufferedOutputStream
 *
 * @see ByteArrayOutputStream
 *
 * @see DataOutputStream
 *
 * @see FilterOutputStream
 *
 * @see InputStream
 *
 * @see OutputStream.write
 * @since   1.0
 */
abstract class OutputStream
/**
 * Constructor for subclasses to call.
 */
    : Closeable, Flushable {
    /**
     * Writes the specified byte to this output stream. The general
     * contract for `write` is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument `b`. The 24
     * high-order bits of `b` are ignored.
     *
     *
     * Subclasses of `OutputStream` must provide an
     * implementation for this method.
     *
     * @param      b   the `byte`.
     * @throws IOException  if an I/O error occurs. In particular,
     * an `IOException` may be thrown if the
     * output stream has been closed.
     */
    @Throws(IOException::class)
    abstract fun write(b: Int)

    /**
     * Writes `b.length` bytes from the specified byte array
     * to this output stream. The general contract for `write(b)`
     * is that it should have exactly the same effect as the call
     * `write(b, 0, b.length)`.
     *
     * @param      b   the data.
     * @throws IOException  if an I/O error occurs.
     * @see OutputStream.write
     */
    @Throws(IOException::class)
    fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    /**
     * Writes `len` bytes from the specified byte array
     * starting at offset `off` to this output stream.
     * The general contract for `write(b, off, len)` is that
     * some of the bytes in the array `b` are written to the
     * output stream in order; element `b[off]` is the first
     * byte written and `b[off+len-1]` is the last byte written
     * by this operation.
     *
     *
     * The `write` method of `OutputStream` calls
     * the write method of one argument on each of the bytes to be
     * written out. Subclasses are encouraged to override this method and
     * provide a more efficient implementation.
     *
     *
     * If `b` is `null`, a
     * `NullPointerException` is thrown.
     *
     *
     * If `off` is negative, or `len` is negative, or
     * `off+len` is greater than the length of the array
     * `b`, then an `IndexOutOfBoundsException` is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @throws IOException  if an I/O error occurs. In particular,
     * an `IOException` is thrown if the output
     * stream is closed.
     */
    @Throws(IOException::class)
    open fun write(b: ByteArray, off: Int, len: Int) {
        checkFromIndexSize(off, len, b.size)
        // len == 0 condition implicitly handled by loop bounds
        for (i in 0 until len) {
            write(b[off + i].toInt())
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of `flush` is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     *
     *
     * If the intended destination of this stream is an abstraction provided by
     * the underlying operating system, for example a file, then flushing the
     * stream guarantees only that bytes previously written to the stream are
     * passed to the operating system for writing; it does not guarantee that
     * they are actually written to a physical device such as a disk drive.
     *
     *
     * The `flush` method of `OutputStream` does nothing.
     *
     * @throws IOException  if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun flush() {
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of `close`
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     *
     *
     * The `close` method of `OutputStream` does nothing.
     *
     * @throws IOException  if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun close() {
    }

    companion object {
        /**
         * Returns a new `OutputStream` which discards all bytes.  The
         * returned stream is initially open.  The stream is closed by calling
         * the `close()` method.  Subsequent calls to `close()` have
         * no effect.
         *
         *
         *  While the stream is open, the `write(int)`, `write(byte[])`, and `write(byte[], int, int)` methods do nothing.
         * After the stream has been closed, these methods all throw `IOException`.
         *
         *
         *  The `flush()` method does nothing.
         *
         * @return an `OutputStream` which discards all bytes
         *
         * @since 11
         */
        fun nullOutputStream(): OutputStream {
            return object : OutputStream() {
                @Volatile
                private var closed = false
                @Throws(IOException::class)
                private fun ensureOpen() {
                    if (closed) {
                        throw IOException("Stream closed")
                    }
                }

                @Throws(IOException::class)
                override fun write(b: Int) {
                    ensureOpen()
                }

                @Throws(IOException::class)
                override fun write(b: ByteArray, off: Int, len: Int) {
                    checkFromIndexSize(off, len, b.size)
                    ensureOpen()
                }

                override fun close() {
                    closed = true
                }
            }
        }
    }
}