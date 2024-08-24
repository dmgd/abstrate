package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ComparablesKtTests {

    @Test
    fun `don't cap values under maximum`() {
        assertEquals(3, 3.cappedAt(5))
    }

    @Test
    fun `cap values over maximum`() {
        assertEquals(5, 7.cappedAt(5))
    }

    @Test
    fun `capped values can be equal to the maximum `() {
        assertEquals(5, 5.cappedAt(5))
    }
}
