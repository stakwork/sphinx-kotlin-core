package org.bouncycastle_ktx.crypto.digests

import org.bouncycastle_ktx.crypto.ExtendedDigest
import org.bouncycastle_ktx.util.Memoable
import org.bouncycastle_ktx.util.Pack

/**
 * base implementation of MD4 family style digest as outlined in
 * "Handbook of Applied Cryptography", pages 344 - 347.
 * */
abstract class GeneralDigest: ExtendedDigest, Memoable {
    private val xBuf = ByteArray(4)
    private var xBufOff = 0
    private var byteCount: Long = 0

    /**
     * Standard constructor
     * */
    protected constructor() {
        xBufOff = 0
    }

    /**
     * Copy constructor.  We are using copy constructors in place
     * of the Object.clone() interface as this interface is not
     * supported by J2ME.
     * */
    protected constructor(t: GeneralDigest) {
        copyIn(t)
    }

    protected constructor(encodedState: ByteArray) {
        System.arraycopy(encodedState, 0, xBuf, 0, xBuf.size)
        xBufOff = Pack.bigEndianToInt(encodedState, 4)
        byteCount = Pack.bigEndianToLong(encodedState, 8)
    }

    protected fun copyIn(t: GeneralDigest) {
        System.arraycopy(t.xBuf, 0, xBuf, 0, t.xBuf.size)
        xBufOff = t.xBufOff
        byteCount = t.byteCount
    }

    override fun update(`in`: Byte) {
        xBuf[xBufOff++] = `in`
        if (xBufOff == xBuf.size) {
            processWord(xBuf, 0)
            xBufOff = 0
        }
        byteCount++
    }

    override fun update(`in`: ByteArray, inOff: Int, len: Int) {
        var lenVar = len
        lenVar = Math.max(0, lenVar)

        //
        // fill the current word
        //
        var i = 0
        if (xBufOff != 0) {
            while (i < lenVar) {
                xBuf[xBufOff++] = `in`[inOff + i++]
                if (xBufOff == 4) {
                    processWord(xBuf, 0)
                    xBufOff = 0
                    break
                }
            }
        }

        //
        // process whole words.
        //
        val limit = (lenVar - i and 3.inv()) + i
        while (i < limit) {
            processWord(`in`, inOff + i)
            i += 4
        }

        //
        // load in the remainder.
        //
        while (i < lenVar) {
            xBuf[xBufOff++] = `in`[inOff + i++]
        }
        byteCount += lenVar.toLong()
    }

    fun finish() {
        val bitLength = byteCount shl 3

        //
        // add the pad bytes.
        //
        update(128.toByte())
        while (xBufOff != 0) {
            update(0.toByte())
        }
        processLength(bitLength)
        processBlock()
    }

    override fun reset() {
        byteCount = 0
        xBufOff = 0
        for (i in xBuf.indices) {
            xBuf[i] = 0
        }
    }

    protected fun populateState(state: ByteArray) {
        System.arraycopy(xBuf, 0, state, 0, xBufOff)
        Pack.intToBigEndian(xBufOff, state, 4)
        Pack.longToBigEndian(byteCount, state, 8)
    }

    override fun getByteLength(): Int {
        return BYTE_LENGTH
    }

    protected abstract fun processWord(`in`: ByteArray, inOff: Int)
    protected abstract fun processLength(bitLength: Long)
    protected abstract fun processBlock()

    companion object {
        private const val BYTE_LENGTH = 64
    }
}
