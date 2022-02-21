package com.stakwork.koi

import kotlinx.io.errors.IOException
import kotlin.math.max

var MAX_ARRAY_LENGTH = Int.MAX_VALUE - 8

/**
 * Calculates a new array length given an array's current length, a preferred
 * growth value, and a minimum growth value.  If the preferred growth value
 * is less than the minimum growth value, the minimum growth value is used in
 * its place.  If the sum of the current length and the preferred growth
 * value does not exceed [.MAX_ARRAY_LENGTH], that sum is returned.
 * If the sum of the current length and the minimum growth value does not
 * exceed `MAX_ARRAY_LENGTH`, then `MAX_ARRAY_LENGTH` is returned.
 * If the sum does not overflow an int, then `Integer.MAX_VALUE` is
 * returned.  Otherwise, `OutOfMemoryError` is thrown.
 *
 * @param oldLength   current length of the array (must be non negative)
 * @param minGrowth   minimum required growth of the array length (must be
 * positive)
 * @param prefGrowth  preferred growth of the array length (ignored, if less
 * then `minGrowth`)
 * @return the new length of the array
 * @throws OutOfMemoryError if increasing `oldLength` by
 * `minGrowth` overflows.
 */
fun newLength(oldLength: Int, minGrowth: Int, prefGrowth: Int): Int {
    // assert oldLength >= 0
    // assert minGrowth > 0
    val newLength = max(minGrowth, prefGrowth) + oldLength
    return if (newLength - MAX_ARRAY_LENGTH <= 0) {
        newLength
    } else hugeLength(oldLength, minGrowth)
}

private fun hugeLength(oldLength: Int, minGrowth: Int): Int {
    val minLength = oldLength + minGrowth
    if (minLength < 0) { // overflow
        throw IOException("Required array length too large")
    }
    return if (minLength <= MAX_ARRAY_LENGTH) {
        MAX_ARRAY_LENGTH
    } else Int.MAX_VALUE
}