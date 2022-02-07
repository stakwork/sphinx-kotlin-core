package org.bouncycastle_ktx.crypto

/**
 * The base interface for implementations of message authentication codes (MACs).
 * */
interface Mac {
    /**
     * Initialise the MAC.
     *
     * @param params the key and other data required by the MAC.
     * @exception IllegalArgumentException if the params argument is
     * inappropriate.
     * */
    @Throws(IllegalArgumentException::class)
    fun init(params: CipherParameters)

    /**
     * Return the name of the algorithm the MAC implements.
     *
     * @return the name of the algorithm the MAC implements.
     * */
    fun getAlgorithmName(): String

    /**
     * Return the block size for this MAC (in bytes).
     *
     * @return the block size for this MAC in bytes.
     * */
    fun getMacSize(): Int

    /**
     * add a single byte to the mac for processing.
     *
     * @param in the byte to be processed.
     * @exception IllegalStateException if the MAC is not initialised.
     * */
    @Throws(IllegalStateException::class)
    fun update(`in`: Byte)

    /**
     * @param in the array containing the input.
     * @param inOff the index in the array the data begins at.
     * @param len the length of the input starting at inOff.
     * @exception IllegalStateException if the MAC is not initialised.
     * @exception DataLengthException if there isn't enough data in in.
     * */
    @Throws(DataLengthException::class, IllegalStateException::class)
    fun update(`in`: ByteArray, inOff: Int, len: Int)

    /**
     * Compute the final stage of the MAC writing the output to the out
     * parameter.
     *
     *
     * doFinal leaves the MAC in the same state it was after the last init.
     *
     * @param out the array the MAC is to be output to.
     * @param outOff the offset into the out buffer the output is to start at.
     * @exception DataLengthException if there isn't enough space in out.
     * @exception IllegalStateException if the MAC is not initialised.
     * */
    @Throws(DataLengthException::class, IllegalStateException::class)
    fun doFinal(out: ByteArray, outOff: Int): Int

    /**
     * Reset the MAC. At the end of resetting the MAC should be in the
     * in the same state it was after the last init (if there was one).
     * */
    fun reset()
}
