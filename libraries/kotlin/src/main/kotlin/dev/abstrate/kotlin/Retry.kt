package dev.abstrate.kotlin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import kotlin.time.Duration

fun <T> retry(
    initial: T,
    attempt: () -> T,
    until: (T) -> Boolean,
    attemptDelays: Sequence<Duration>,
    sleep: (Duration) -> Unit = { Thread.sleep(it.inWholeMilliseconds) },
): Result<T, TimedOut> {
    if (until(initial)) {
        return Success(initial)
    }
    return retry(attempt, until, attemptDelays, sleep)
}

fun <T> retry(
    attempt: () -> T,
    until: (T) -> Boolean,
    attemptDelays: Sequence<Duration>,
    sleep: (Duration) -> Unit = { Thread.sleep(it.inWholeMilliseconds) },
): Result<T, TimedOut> {
    for (delay in attemptDelays) {
        sleep(delay)
        val candidate = attempt()
        if (until(candidate)) {
            return Success(candidate)
        }
    }
    return Failure(TimedOut)
}

val immediately = sequenceOf(Duration.ZERO)

data object TimedOut
