package org.bouncycastle_ktx.crypto

/**
 * the foundation class for the exceptions thrown by the crypto packages.
 * */
open class RuntimeCryptoException: RuntimeException {
    /**
     * base constructor.
     * */
    constructor()

    /**
     * create a RuntimeCryptoException with the given message.
     *
     * @param message the message to be carried with the exception.
     * */
    constructor(message: String?): super(message)
}