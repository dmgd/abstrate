package dev.abstrate.kotlin

import java.time.Instant
import java.util.EnumSet
import java.util.random.RandomGenerator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds

fun <T> RandomGenerator.nextFrom(collection: Collection<T>): T =
    if (collection.isEmpty()) {
        throw IllegalArgumentException("Collection is empty")
    } else {
        collection.elementAt(nextInt(collection.size))
    }

fun <T> RandomGenerator.valuesFrom(collection: Collection<T>): Sequence<T> =
    if (collection.isEmpty()) {
        throw IllegalArgumentException("Collection is empty")
    } else {
        generateSequence {
            nextFrom(collection)
        }
    }

inline fun <reified T : Enum<T>> RandomGenerator.nextFrom(values: EnumSet<T> = EnumSet.allOf(T::class.java)): T =
    if (values.isEmpty()) {
        throw IllegalArgumentException("${T::class.simpleName} has no values")
    } else {
        values.elementAt(nextInt(values.size))
    }

inline fun <reified T : Enum<T>> RandomGenerator.valuesFrom(values: EnumSet<T> = EnumSet.allOf(T::class.java)): Sequence<T> =
    if (values.isEmpty()) {
        throw IllegalArgumentException("${T::class.simpleName} has no values")
    } else {
        generateSequence {
            nextFrom(values)
        }
    }

fun RandomGenerator.chars(allowedCharacters: Set<Char>): Sequence<Char> =
    generateSequence {
        nextFrom(allowedCharacters)
    }

fun RandomGenerator.strings(
    allowedCharacters: Set<Char>,
    minLength: Int = 0,
    maxLength: Int = 80,
): Sequence<String> {
    require(0 <= minLength) { "0 must be <= minLength ($minLength)" }
    require(minLength <= maxLength) { "minLength ($minLength) must be <= maxLength ($maxLength)" }
    return generateSequence {
        chars(allowedCharacters).take(nextInt(minLength, maxLength + 1))
            .joinToString("")
    }
}

fun RandomGenerator.nextDuration(
    min: Duration = Duration.ZERO,
    max: Duration = 10.days,
): Duration {
    require(min <= max) { "min ($min) must be <= max ($max)" }
    return nextLong(min.inWholeNanoseconds, max.inWholeNanoseconds + 1).nanoseconds
}

fun RandomGenerator.durations(
    min: Duration = Duration.ZERO,
    max: Duration = 10.days,
) =
    generateSequence {
        nextDuration(min = min, max = max)
    }

fun RandomGenerator.timeline(
    earliestStart: Instant = Instant.EPOCH,
    minStep: Duration,
    maxStep: Duration,
): Sequence<Instant> =
    durations(minStep, maxStep)
        .runningFold(earliestStart) { acc, next -> acc.plusNanos(next.inWholeNanoseconds) }
        .drop(1)
