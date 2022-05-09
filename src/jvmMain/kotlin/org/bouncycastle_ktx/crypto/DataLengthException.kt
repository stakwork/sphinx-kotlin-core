package org.bouncycastle_ktx.crypto

/**
 * this exception is thrown if a buffer that is meant to have output
 * copied into it turns out to be too short, or if we've been given
 * insufficient input. In general this exception will get thrown rather
 * than an ArrayOutOfBounds exception.
 * */
open class DataLengthException : RuntimeCryptoException {
    /**
     * base constructor.
     * */
    constructor()

    /**
     * create a DataLengthException with the given message.
     *
     * @param message the message to be carried with the exception.
     * */
    constructor(message: String?): super(message)
}
