package chat.sphinx.utils

import io.ktor.http.*

fun checkFromIndexSize(fromIndex: Int, size: Int, length: Int): Int {
    return if (length or fromIndex or size >= 0 && size <= length - fromIndex) {
        fromIndex
    } else {
        throw IndexOutOfBoundsException("Range [$fromIndex, $size + $length) out of bounds for length $length")
    }
}

fun checkFromToIndex(fromIndex: Int, toIndex: Int, length: Int): Int {
    return if (fromIndex in 0..toIndex && toIndex <= length) {
        fromIndex
    } else {
        throw IndexOutOfBoundsException("Range [$fromIndex, $toIndex) out of bounds for length $length")
    }
}

inline fun String.toHttpUrlOrNull(): Url? {
    return try {
        Url(this)
    } catch (e: URLParserException) {
        null
    }
}