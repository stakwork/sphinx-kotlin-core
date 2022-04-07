package org.bouncycastle_ktx.crypto.macs

import org.bouncycastle_ktx.crypto.CipherParameters
import org.bouncycastle_ktx.crypto.Digest
import org.bouncycastle_ktx.crypto.ExtendedDigest
import org.bouncycastle_ktx.crypto.Mac
import org.bouncycastle_ktx.crypto.params.KeyParameter
import org.bouncycastle_ktx.util.Integers
import org.bouncycastle_ktx.util.Memoable
import java.util.Hashtable
import kotlin.experimental.xor

/**
 * HMAC implementation based on RFC2104
 *
 * H(K XOR opad, H(K XOR ipad, text))
 * */
class HMac private constructor(val underlyingDigest: Digest, byteLength: Int) : Mac {
    private val digestSize: Int = underlyingDigest.getDigestSize()
    private val blockLength: Int = byteLength
    private var ipadState: Memoable? = null
    private var opadState: Memoable? = null
    private val inputPad: ByteArray = ByteArray(blockLength)
    private val outputBuf: ByteArray = ByteArray(blockLength + digestSize)

    companion object {
        private const val IPAD = 0x36.toByte()
        private const val OPAD = 0x5C.toByte()
        private fun getByteLength(digest: Digest): Int {
            return if (digest is ExtendedDigest) {
                digest.getByteLength()
            } else blockLengths[digest.getAlgorithmName()]
                ?: throw IllegalArgumentException("unknown digest passed: " + digest.getAlgorithmName())
        }

        private fun xorPad(pad: ByteArray, len: Int, n: Byte) {
            for (i in 0 until len) {
                pad[i] = pad[i] xor n
            }
        }

        private val blockLengths = Hashtable(
            mapOf(
//                Pair("GOST3411", Integers.valueOf(32)),
//                Pair("MD2", Integers.valueOf(16)),
//                Pair("MD4", Integers.valueOf(64)),
//                Pair("MD5", Integers.valueOf(64)),
//                Pair("RIPEMD128", Integers.valueOf(64)),
//                Pair("RIPEMD160", Integers.valueOf(64)),
//                Pair("SHA-1", Integers.valueOf(64)),
//                Pair("SHA-224", Integers.valueOf(64)),
                Pair("SHA-256", Integers.valueOf(64)),
//                Pair("SHA-384", Integers.valueOf(128)),
//                Pair("SHA-512", Integers.valueOf(128)),
//                Pair("Tiger", Integers.valueOf(64)),
//                Pair("Whirlpool", Integers.valueOf(64))
            )
        )
    }

    /**
     * Base constructor for one of the standard digest algorithms that the
     * byteLength of the algorithm is know for.
     *
     * @param digest the digest.
     * */
    constructor(digest: Digest): this(digest, getByteLength(digest))

    override fun getAlgorithmName(): String {
        return underlyingDigest.getAlgorithmName() + "/HMAC"
    }

    override fun init(params: CipherParameters) {
        underlyingDigest.reset()
        val key = (params as KeyParameter).key
        var keyLength = key.size
        if (keyLength > blockLength) {
            underlyingDigest.update(key, 0, keyLength)
            underlyingDigest.doFinal(inputPad, 0)
            keyLength = digestSize
        } else {
            System.arraycopy(key, 0, inputPad, 0, keyLength)
        }
        for (i in keyLength until inputPad.size) {
            inputPad[i] = 0
        }
        System.arraycopy(inputPad, 0, outputBuf, 0, blockLength)
        xorPad(inputPad, blockLength, IPAD)
        xorPad(outputBuf, blockLength, OPAD)
        if (underlyingDigest is Memoable) {
            opadState = (underlyingDigest as Memoable).copy()
            (opadState as Digest).update(outputBuf, 0, blockLength)
        }
        underlyingDigest.update(inputPad, 0, inputPad.size)
        if (underlyingDigest is Memoable) {
            ipadState = (underlyingDigest as Memoable).copy()
        }
    }

    override fun getMacSize(): Int {
        return digestSize
    }

    override fun update(`in`: Byte) {
        underlyingDigest.update(`in`)
    }

    override fun update(`in`: ByteArray, inOff: Int, len: Int) {
        underlyingDigest.update(`in`, inOff, len)
    }

    override fun doFinal(out: ByteArray, outOff: Int): Int {
        underlyingDigest.doFinal(outputBuf, blockLength)
        opadState?.let { nnOpadState ->
            (underlyingDigest as Memoable).reset(nnOpadState)
            underlyingDigest.update(outputBuf, blockLength, underlyingDigest.getDigestSize())
        } ?: underlyingDigest.update(outputBuf, 0, outputBuf.size)
        val len = underlyingDigest.doFinal(out, outOff)
        for (i in blockLength until outputBuf.size) {
            outputBuf[i] = 0
        }
        ipadState?.let { nnIPadState ->
            (underlyingDigest as Memoable).reset(nnIPadState)
        } ?: underlyingDigest.update(inputPad, 0, inputPad.size)

        return len
    }

    /**
     * Reset the mac generator.
     * */
    override fun reset() {
        /*
         * reset the underlying digest.
         */
        underlyingDigest.reset()

        /*
         * reinitialize the digest.
         */
        underlyingDigest.update(inputPad, 0, inputPad.size)
    }

}
