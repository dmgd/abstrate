package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant.EPOCH
import java.util.Random
import kotlin.time.Duration.Companion.hours

class RandomKtTests {

    @Test
    fun `can't take next from empty collection`() {
        assertThrows<IllegalArgumentException> {
            Random().nextFrom(emptyList())
        }
    }

    @Test
    fun `take next from list`() {
        assertEquals(1, Random().nextFrom(listOf(1, 1, 1)))
    }

    @Test
    fun `take next from set`() {
        assertEquals(1, Random().nextFrom(setOf(1)))
    }

    @Test
    fun `collection values`() {
        assertEquals(listOf(1, 1), Random().valuesFrom(setOf(1)).take(2).toList())
    }

    @Test
    fun `take next enum`() {
        assertEquals(TestEnum.A, Random().nextFrom<TestEnum>())
    }

    @Test
    fun `enum values`() {
        assertEquals(listOf(TestEnum.A, TestEnum.A), Random().valuesFrom<TestEnum>().take(2).toList())
    }

    @Test
    fun chars() {
        assertEquals("aaa", Random().chars(setOf('a')).take(3).joinToString(""))
    }

    @Test
    fun strings() {
        assertEquals("aaa|aaa", Random().strings(setOf('a'), minLength = 3, maxLength = 3).take(2).joinToString("|"))
    }

    @Test
    fun `next duration`() {
        assertEquals(1.hours, Random().nextDuration(min = 1.hours, max = 1.hours))
    }

    @Test
    fun durations() {
        assertEquals(
            listOf(
                1.hours,
                1.hours,
                1.hours,
            ),
            Random().durations(min = 1.hours, max = 1.hours)
                .take(3)
                .toList()
        )
    }

    @Test
    fun timeline() {
        assertEquals(
            listOf(
                EPOCH + 1.hours,
                EPOCH + 2.hours,
                EPOCH + 3.hours,
            ),
            Random().timeline(earliestStart = EPOCH, minStep = 1.hours, maxStep = 1.hours)
                .take(3)
                .toList()
        )
    }

    enum class TestEnum {
        A
    }
}
