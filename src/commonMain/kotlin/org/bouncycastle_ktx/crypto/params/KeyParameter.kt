package org.bouncycastle_ktx.crypto.params

import org.bouncycastle_ktx.crypto.CipherParameters
import kotlin.jvm.JvmOverloads

open class KeyParameter @JvmOverloads constructor(
    key: ByteArray,
    keyOff: Int = 0,
    keyLen: Int = key.size
) : CipherParameters {
    val key: ByteArray = ByteArray(keyLen)

    init {
        key.copyInto(this.key, 0, keyOff, keyLen)
    }
}
