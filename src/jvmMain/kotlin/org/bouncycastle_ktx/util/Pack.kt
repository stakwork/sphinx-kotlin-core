package org.bouncycastle_ktx.util

/**
 * Utility methods for converting byte arrays into ints and longs, and back again.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object Pack {
    fun bigEndianToShort(bs: ByteArray, off: Int): Short {
        var offVar = off
        var n: Int = bs[offVar].toInt() and 0xff shl 8
        n = n or (bs[++offVar].toInt() and 0xff)
        return n.toShort()
    }

    fun bigEndianToInt(bs: ByteArray, off: Int): Int {
        var offVar = off
        var n: Int = bs[offVar].toInt() shl 24
        n = n or (bs[++offVar].toInt() and 0xff shl 16)
        n = n or (bs[++offVar].toInt() and 0xff shl 8)
        n = n or (bs[++offVar].toInt() and 0xff)
        return n
    }

    fun bigEndianToInt(bs: ByteArray, off: Int, ns: IntArray) {
        var offVar = off
        for (i in ns.indices) {
            ns[i] = bigEndianToInt(bs, offVar)
            offVar += 4
        }
    }

    fun intToBigEndian(n: Int): ByteArray {
        val bs = ByteArray(4)
        intToBigEndian(n, bs, 0)
        return bs
    }

    fun intToBigEndian(n: Int, bs: ByteArray, off: Int) {
        var offVar = off
        bs[offVar] = (n ushr 24).toByte()
        bs[++offVar] = (n ushr 16).toByte()
        bs[++offVar] = (n ushr 8).toByte()
        bs[++offVar] = n.toByte()
    }

    fun intToBigEndian(ns: IntArray): ByteArray {
        val bs = ByteArray(4 * ns.size)
        intToBigEndian(ns, bs, 0)
        return bs
    }

    fun intToBigEndian(ns: IntArray, bs: ByteArray, off: Int) {
        var offVar = off
        for (i in ns.indices) {
            intToBigEndian(ns[i], bs, offVar)
            offVar += 4
        }
    }

    fun bigEndianToLong(bs: ByteArray, off: Int): Long {
        val hi = bigEndianToInt(bs, off).toLong()
        val lo = bigEndianToInt(bs, off + 4).toLong()
        return (hi and 0xffffffffL) shl 32 or (lo and 0xffffffffL)
    }

    fun bigEndianToLong(bs: ByteArray, off: Int, ns: LongArray) {
        var offVar = off
        for (i in ns.indices) {
            ns[i] = bigEndianToLong(bs, offVar)
            offVar += 8
        }
    }

    fun longToBigEndian(n: Long): ByteArray {
        val bs = ByteArray(8)
        longToBigEndian(n, bs, 0)
        return bs
    }

    fun longToBigEndian(n: Long, bs: ByteArray, off: Int) {
        intToBigEndian((n ushr 32).toInt(), bs, off)
        intToBigEndian((n and 0xffffffffL).toInt(), bs, off + 4)
    }

    fun longToBigEndian(ns: LongArray): ByteArray {
        val bs = ByteArray(8 * ns.size)
        longToBigEndian(ns, bs, 0)
        return bs
    }

    fun longToBigEndian(ns: LongArray, bs: ByteArray, off: Int) {
        var offVar = off
        for (i in ns.indices) {
            longToBigEndian(ns[i], bs, offVar)
            offVar += 8
        }
    }

    /**
     * @param value The number
     * @param bs    The target.
     * @param off   Position in target to start.
     * @param bytes number of bytes to write.
     */
    fun longToBigEndian(value: Long, bs: ByteArray, off: Int, bytes: Int) {
        var valueVar = value
        for (i in bytes - 1 downTo 0) {
            bs[i + off] = (valueVar and 0xff).toByte()
            valueVar = valueVar ushr 8
        }
    }

    fun littleEndianToShort(bs: ByteArray, off: Int): Short {
        var offVar = off
        var n: Int = bs[offVar].toInt() and 0xff
        n = n or (bs[++offVar].toInt() and 0xff shl 8)
        return n.toShort()
    }

    fun littleEndianToInt(bs: ByteArray, off: Int): Int {
        var offVar = off
        var n: Int = bs[offVar].toInt() and 0xff
        n = n or (bs[++offVar].toInt() and 0xff shl 8)
        n = n or (bs[++offVar].toInt() and 0xff shl 16)
        n = n or (bs[++offVar].toInt() shl 24)
        return n
    }

    fun littleEndianToInt(bs: ByteArray, off: Int, ns: IntArray) {
        var offVar = off
        for (i in ns.indices) {
            ns[i] = littleEndianToInt(bs, offVar)
            offVar += 4
        }
    }

    fun littleEndianToInt(bs: ByteArray, bOff: Int, ns: IntArray, nOff: Int, count: Int) {
        var bOffVar = bOff
        for (i in 0 until count) {
            ns[nOff + i] = littleEndianToInt(bs, bOffVar)
            bOffVar += 4
        }
    }

    fun littleEndianToInt(bs: ByteArray, off: Int, count: Int): IntArray {
        var offVar = off
        val ns = IntArray(count)
        for (i in ns.indices) {
            ns[i] = littleEndianToInt(bs, offVar)
            offVar += 4
        }
        return ns
    }

    fun shortToLittleEndian(n: Short): ByteArray {
        val bs = ByteArray(2)
        shortToLittleEndian(n, bs, 0)
        return bs
    }

    fun shortToLittleEndian(n: Short, bs: ByteArray, off: Int) {
        var offVar = off
        bs[offVar] = n.toByte()
        bs[++offVar] = (n.toInt() ushr 8).toByte()
    }

    fun shortToBigEndian(n: Short): ByteArray {
        val r = ByteArray(2)
        shortToBigEndian(n, r, 0)
        return r
    }

    fun shortToBigEndian(n: Short, bs: ByteArray, off: Int) {
        var offVar = off
        bs[offVar] = (n.toInt() ushr 8).toByte()
        bs[++offVar] = n.toByte()
    }

    fun intToLittleEndian(n: Int): ByteArray {
        val bs = ByteArray(4)
        intToLittleEndian(n, bs, 0)
        return bs
    }

    fun intToLittleEndian(n: Int, bs: ByteArray, off: Int) {
        var offVar = off
        bs[offVar] = n.toByte()
        bs[++offVar] = (n ushr 8).toByte()
        bs[++offVar] = (n ushr 16).toByte()
        bs[++offVar] = (n ushr 24).toByte()
    }

    fun intToLittleEndian(ns: IntArray): ByteArray {
        val bs = ByteArray(4 * ns.size)
        intToLittleEndian(ns, bs, 0)
        return bs
    }

    fun intToLittleEndian(ns: IntArray, bs: ByteArray, off: Int) {
        var offVar = off
        for (i in ns.indices) {
            intToLittleEndian(ns[i], bs, offVar)
            offVar += 4
        }
    }

    fun littleEndianToLong(bs: ByteArray, off: Int): Long {
        val lo = littleEndianToInt(bs, off).toLong()
        val hi = littleEndianToInt(bs, off + 4).toLong()
        return (hi and 0xffffffffL) shl 32 or (lo and 0xffffffffL)
    }

    fun littleEndianToLong(bs: ByteArray, off: Int, ns: LongArray) {
        var offVar = off
        for (i in ns.indices) {
            ns[i] = littleEndianToLong(bs, offVar)
            offVar += 8
        }
    }

    fun littleEndianToLong(bs: ByteArray, bsOff: Int, ns: LongArray, nsOff: Int, nsLen: Int) {
        var bsOffVar = bsOff
        for (i in 0 until nsLen) {
            ns[nsOff + i] = littleEndianToLong(bs, bsOffVar)
            bsOffVar += 8
        }
    }

    fun longToLittleEndian(n: Long): ByteArray {
        val bs = ByteArray(8)
        longToLittleEndian(n, bs, 0)
        return bs
    }

    fun longToLittleEndian(n: Long, bs: ByteArray, off: Int) {
        intToLittleEndian((n and 0xffffffffL).toInt(), bs, off)
        intToLittleEndian((n ushr 32).toInt(), bs, off + 4)
    }

    fun longToLittleEndian(ns: LongArray): ByteArray {
        val bs = ByteArray(8 * ns.size)
        longToLittleEndian(ns, bs, 0)
        return bs
    }

    fun longToLittleEndian(ns: LongArray, bs: ByteArray, off: Int) {
        var offVar = off
        for (i in ns.indices) {
            longToLittleEndian(ns[i], bs, offVar)
            offVar += 8
        }
    }

    fun longToLittleEndian(ns: LongArray, nsOff: Int, nsLen: Int, bs: ByteArray, bsOff: Int) {
        var bsOffVar = bsOff
        for (i in 0 until nsLen) {
            longToLittleEndian(ns[nsOff + i], bs, bsOffVar)
            bsOffVar += 8
        }
    }
}
