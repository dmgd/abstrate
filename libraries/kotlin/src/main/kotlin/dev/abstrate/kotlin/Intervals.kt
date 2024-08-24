package dev.abstrate.kotlin

import java.time.Duration

val immediately = sequenceOf(Duration.ZERO)

fun constantIntervals(interval: Duration) =
    generateSequence(interval) {
        it
    }

fun exponentialIntervals(initial: Duration, multiplier: Int = 2) =
    generateSequence(initial) {
        it * multiplier
    }

fun Sequence<Duration>.cappedAt(max: Duration) =
    map {
        it.cappedAt(max)
    }
