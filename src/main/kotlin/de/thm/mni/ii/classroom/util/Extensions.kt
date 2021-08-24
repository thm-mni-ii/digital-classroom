package de.thm.mni.ii.classroom.util

import reactor.util.function.Tuple2

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

/**
 * Convert to Kotlin Pair
 */
fun <T1, T2> Tuple2<T1, T2>.toPair(): Pair<T1, T2> = Pair(t1, t2)

/**
 * Deconstructing operator for the first element of a Tuple2 of project Reactor.
 */
operator fun <T1, T2> Tuple2<T1, T2>.component1(): T1 = t1

/**
 * Deconstructing operators for the second element of a Tuple2 of project Reactor.
 */
operator fun <T1, T2> Tuple2<T1, T2>.component2(): T2 = t2

