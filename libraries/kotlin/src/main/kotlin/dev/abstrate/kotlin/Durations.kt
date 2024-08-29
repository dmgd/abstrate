package dev.abstrate.kotlin

import java.time.Instant
import kotlin.time.Duration

fun Duration.repeating() =
    generateSequence(this) {
        it
    }

fun Duration.exponentialBackoff(multiplier: Int = 2) =
    generateSequence(this) {
        it * multiplier
    }

fun Sequence<Duration>.cappedAt(max: Duration) =
    map {
        it.cappedAt(max)
    }

operator fun Instant.plus(duration: Duration): Instant =
    plusNanos(duration.inWholeNanoseconds)
