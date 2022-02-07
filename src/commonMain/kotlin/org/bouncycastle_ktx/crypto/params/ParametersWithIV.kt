package org.bouncycastle_ktx.crypto.params

import org.bouncycastle_ktx.crypto.CipherParameters
import kotlin.jvm.JvmOverloads

class ParametersWithIV @JvmOverloads constructor(
    val parameters: CipherParameters,
    iv: ByteArray,
    ivOff: Int = 0,
    ivLen: Int = iv.size
): CipherParameters {
    val iV: ByteArray = ByteArray(ivLen)

    init {
        iv.copyInto(iV, 0, ivOff, ivLen)
    }
}
