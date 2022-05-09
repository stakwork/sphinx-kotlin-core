package org.bouncycastle_ktx.util

/**
 * An interface defining a list of strings.
 * */
interface StringList: Iterable<String> {
    /**
     * Add a String to the list.
     *
     * @param element the String to add.
     * @return true
     * */
    fun add(element: String): Boolean

    /**
     * Get the string at index index.
     *
     * @param index the index position of the String of interest.
     * @return the String at position index.
     * */
    operator fun get(index: Int): String
//    fun size(): Int

    /**
     * Return the contents of the list as an array.
     *
     * @return an array of String.
     * */
    fun toStringArray(): Array<String>

    /**
     * Return a section of the contents of the list. If the list is too short the array is filled with nulls.
     *
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     * @return an array of length to - from
     * */
    fun toStringArray(from: Int, to: Int): Array<String>
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringList.size(): Int {
    var count = 0
    this.forEach { _ ->
        count++
    }
    return count
}