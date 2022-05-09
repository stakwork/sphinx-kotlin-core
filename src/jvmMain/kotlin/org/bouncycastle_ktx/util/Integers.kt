package org.bouncycastle_ktx.util

/**
 * Utility methods for ints.
 */
object Integers {
    fun numberOfLeadingZeros(i: Int): Int {
        return Integer.numberOfLeadingZeros(i)
    }

    fun rotateLeft(i: Int, distance: Int): Int {
        return Integer.rotateLeft(i, distance)
    }

    fun rotateRight(i: Int, distance: Int): Int {
        return Integer.rotateRight(i, distance)
    }

    fun valueOf(value: Int): Int {
        return Integer.valueOf(value)
    }
}