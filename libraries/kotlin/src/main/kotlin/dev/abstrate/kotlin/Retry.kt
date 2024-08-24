package dev.abstrate.kotlin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Duration

fun <T> retry(
    initial: T,
    attempt: () -> T,
    until: (T) -> Boolean,
    intervals: Sequence<Duration>,
    sleep: (Duration) -> Unit = { Thread.sleep(it.toMillis()) },
): Result<T, TimedOut> {
    if (until(initial)) {
        return Success(initial)
    }
    return retry(attempt, until, intervals, sleep)
}

fun <T> retry(
    attempt: () -> T,
    until: (T) -> Boolean,
    intervals: Sequence<Duration>,
    sleep: (Duration) -> Unit = { Thread.sleep(it.toMillis()) },
): Result<T, TimedOut> {
    for (interval in intervals) {
        sleep(interval)
        val candidate = attempt()
        if (until(candidate)) {
            return Success(candidate)
        }
    }
    return Failure(TimedOut)
}

data object TimedOut
