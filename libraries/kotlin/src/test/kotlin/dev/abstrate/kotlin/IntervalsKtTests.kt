package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration.ZERO
import java.time.Duration.ofMillis

class IntervalsKtTests {

    @Test
    fun `immediately happens once, right away`() {
        assertEquals(listOf(ZERO), immediately.toList())
    }

    @Test
    fun `constant intervals return the same value forever`() {
        val interval = ofMillis(7)
        val intervals = constantIntervals(interval).iterator()
        repeat(1_000_000) {
            assertTrue(intervals.hasNext(), "hasNext")
            assertEquals(interval, intervals.next())
        }
    }

    @Test
    fun `exponential intervals continue forever`() {
        val interval = ofMillis(7)
        val intervals = exponentialIntervals(interval, multiplier = 1).iterator()
        repeat(1_000_000) {
            assertTrue(intervals.hasNext(), "hasNext")
            assertEquals(interval, intervals.next())
        }
    }

    @Test
    fun `expontential intervals increase by the multiplier each time`() {
        val result =
            exponentialIntervals(
                ofMillis(7),
                multiplier = 2
            )
                .take(5)
                .toList()
        assertEquals(
            listOf(
                ofMillis(7),
                ofMillis(14),
                ofMillis(28),
                ofMillis(56),
                ofMillis(112),
            ),
            result
        )
    }

    @Test
    fun `capped intervals dont exceed the maximum`() {
        val result =
            exponentialIntervals(
                ofMillis(7),
                multiplier = 2
            )
                .cappedAt(ofMillis(20))
                .take(5)
                .toList()
        assertEquals(
            listOf(
                ofMillis(7),
                ofMillis(14),
                ofMillis(20),
                ofMillis(20),
                ofMillis(20),
            ),
            result
        )
    }
}
