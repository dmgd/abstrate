package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class DurationsKtTests {

    @Test
    fun `repeated durations return the same value forever`() {
        val duration = 7.milliseconds
        val repetitions = duration.repeating().iterator()
        repeat(1_000_000) {
            assertTrue(repetitions.hasNext(), "hasNext")
            assertEquals(duration, repetitions.next())
        }
    }

    @Test
    fun `exponential backoff continues forever`() {
        val initial = 7.milliseconds
        val backoff = initial.exponentialBackoff(multiplier = 1).iterator()
        repeat(1_000_000) {
            assertTrue(backoff.hasNext(), "hasNext")
            assertEquals(initial, backoff.next())
        }
    }

    @Test
    fun `exponentially backed-off durations increase by the multiplier each time`() {
        val result =
            7.milliseconds
                .exponentialBackoff(multiplier = 2)
                .take(5)
                .toList()
        assertEquals(
            listOf(
                7.milliseconds,
                14.milliseconds,
                28.milliseconds,
                56.milliseconds,
                112.milliseconds,
            ),
            result
        )
    }

    @Test
    fun `capped durations dont exceed the maximum`() {
        val result =
            7.milliseconds
                .exponentialBackoff(multiplier = 2)
                .cappedAt(20.milliseconds)
                .take(5)
                .toList()
        assertEquals(
            listOf(
                7.milliseconds,
                14.milliseconds,
                20.milliseconds,
                20.milliseconds,
                20.milliseconds,
            ),
            result
        )
    }
}
