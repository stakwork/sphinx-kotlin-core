package org.bouncycastle_ktx.crypto.params

import org.bouncycastle_ktx.crypto.CipherParameters

open class KeyParameter @JvmOverloads constructor(
    key: ByteArray,
    keyOff: Int = 0,
    keyLen: Int = key.size
) : CipherParameters {
    val key: ByteArray = ByteArray(keyLen)

    init {
        System.arraycopy(key, keyOff, this.key, 0, keyLen)
    }
}
