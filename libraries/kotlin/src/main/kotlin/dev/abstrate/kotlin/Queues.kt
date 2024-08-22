package dev.abstrate.kotlin

import java.util.Queue

fun <T> Queue<T>.drainTo(sink: (T) -> Unit) {
    generateSequence(this::poll)
        .forEach(sink)
}
