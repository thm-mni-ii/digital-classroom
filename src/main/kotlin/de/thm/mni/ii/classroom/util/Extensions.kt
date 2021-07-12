package de.thm.mni.ii.classroom.util

/**
 * Repeats a CharSequence until length n. Shortens the string if its length is greater than n.
 */
fun CharSequence.repeatLength(n: Int): String {
    val str = this.repeat(2)
    return if (str.length >= n) {
        str.substring(0 until n)
    } else {
        str.repeatLength(n)
    }
}


fun <T> MutableSet<T>.update(item: T): Boolean {
    this.remove(item)
    return this.add(item)
}