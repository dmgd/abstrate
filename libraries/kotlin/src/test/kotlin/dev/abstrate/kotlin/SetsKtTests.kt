package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SetsKtTests {

    @Test
    fun `Iterable mapToSet`() {
        assertEquals(
            setOf(1, 2, null, 3),
            listOf(2, 1, 2, null, 3, 3)
                .mapToSet { it },
        )
    }

    @Test
    fun `Iterable mapToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 1, null, 3),
            listOf(2, 1, 2, null, 3, 3)
                .mapToSet { it }
                .toList(),
        )
    }

    @Test
    fun `Iterable mapNotNullToSet`() {
        assertEquals(
            setOf(1, 2, 3),
            listOf(2, 1, 2, null, 3, 3)
                .mapNotNullToSet { it },
        )
    }

    @Test
    fun `Iterable mapNotNullToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 1, 3),
            listOf(2, 1, 2, null, 3, 3)
                .mapNotNullToSet { it }
                .toList(),
        )
    }

    @Test
    fun `Iterable flatMapToSet`() {
        assertEquals(
            setOf(1, 2, 3, 4),
            listOf(2, 1, 2, null, 3, 3)
                .flatMapToSet { listOfNotNull(it, it?.plus(1)) },
        )
    }

    @Test
    fun `Iterable flatMapToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 3, 1, 4),
            listOf(2, 1, 2, null, 3, 3)
                .flatMapToSet { listOfNotNull(it, it?.plus(1)) }
                .toList(),
        )
    }

    @Test
    fun `Map mapToSet`() {
        assertEquals(
            setOf(1, 2, null, 3),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .mapToSet { it.key },
        )
    }

    @Test
    fun `Map mapToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 1, null, 3),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .mapToSet { it.key }
                .toList(),
        )
    }

    @Test
    fun `Map mapNotNullToSet`() {
        assertEquals(
            setOf(1, 2, 3),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .mapNotNullToSet { it.key },
        )
    }

    @Test
    fun `Map mapNotNullToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 1, 3),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .mapNotNullToSet { it.key }
                .toList(),
        )
    }

    @Test
    fun `Map flatMapToSet`() {
        assertEquals(
            setOf(1, 2, 3, 4),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .flatMapToSet { listOfNotNull(it.key, it.key?.plus(1)) },
        )
    }

    @Test
    fun `Map flatMapToSet maintains insertion order`() {
        assertEquals(
            listOf(2, 3, 1, 4),
            mapOf(2 to 0, 1 to 0, 2 to 0, null to 0, 3 to 0, 3 to 0)
                .flatMapToSet { listOfNotNull(it.key, it.key?.plus(1)) }
                .toList(),
        )
    }
}
