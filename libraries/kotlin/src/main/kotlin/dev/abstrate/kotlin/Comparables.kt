package dev.abstrate.kotlin

fun <T : Comparable<T>> T.cappedAt(max: T): T =
    if (this < max) this else max
