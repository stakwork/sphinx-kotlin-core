package org.bouncycastle_ktx.util

/**
 * Exception to be thrown on a failure to reset an object implementing Memoable.
 *
 * The exception extends ClassCastException to enable users to have a single handling case,
 * only introducing specific handling of this one if required.
 *
 * @param msg message to be associated with this exception.
 */
class MemoableResetException(msg: String?) : ClassCastException(msg)
