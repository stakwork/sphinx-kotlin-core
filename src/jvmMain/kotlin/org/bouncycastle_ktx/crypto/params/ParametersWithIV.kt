package org.bouncycastle_ktx.crypto.params

import org.bouncycastle_ktx.crypto.CipherParameters

class ParametersWithIV @JvmOverloads constructor(
    val parameters: CipherParameters,
    iv: ByteArray,
    ivOff: Int = 0,
    ivLen: Int = iv.size
): CipherParameters {
    val iV: ByteArray = ByteArray(ivLen)

    init {
        System.arraycopy(iv, ivOff, iV, 0, ivLen)
    }
}
