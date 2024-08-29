package dev.abstrate.kotlin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.onFailure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class RetryKtTests {

    @Test
    fun `use the initial value if it meets the required condition`() {
        val result =
            retry(
                initial = 1,
                attempt = { fail("unexpected attempt") },
                until = { it == 1 },
                attemptDelays = immediately,
                sleep = { fail("unexpected sleep") }
            ).onFailure { fail("unexpected failure") }
        assertEquals(1, result)
    }

    @Test
    fun `retry if the initial value does not meet the required condition`() {
        var sleeps = 0
        val result =
            retry(
                initial = 1,
                attempt = { 2 },
                until = { it == 2 },
                attemptDelays = immediately,
                sleep = { sleeps++ }
            ).onFailure { fail("unexpected failure") }
        assertEquals(2, result)
        assertEquals(1, sleeps)
    }

    @Test
    fun `retry until the candidate value meets the required condition`() {
        var sleeps = 0
        val result =
            retry(
                initial = 1,
                attempt = { sleeps },
                until = { it == 5 },
                attemptDelays = 1.hours.repeating(),
                sleep = { sleeps++ }
            ).onFailure { fail("unexpected failure") }
        assertEquals(5, result)
        assertEquals(5, sleeps)
    }

    @Test
    fun `give up when retry intervals run out`() {
        var sleeps = 0
        val result =
            retry(
                initial = 1,
                attempt = { sleeps },
                until = { it == 5 },
                attemptDelays = 1.hours.repeating().take(3),
                sleep = { sleeps++ }
            )
        assertEquals(Failure(TimedOut), result)
        assertEquals(3, sleeps)
    }

    @Test
    fun `sleep based on retry intervals`() {
        val sleeps = mutableListOf<Duration>()
        retry(
            initial = 1,
            attempt = { sleeps },
            until = { it == 5 },
            attemptDelays = 2.hours.exponentialBackoff(multiplier = 5).take(3),
            sleep = { sleeps += it }
        )
        assertEquals(listOf(2.hours, 10.hours, 50.hours), sleeps)
    }
}
