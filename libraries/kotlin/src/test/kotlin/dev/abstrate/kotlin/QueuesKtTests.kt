package dev.abstrate.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue

class QueuesKtTests {

    @Test
    fun empty() {
        val sink = mutableListOf<Int>()
        ConcurrentLinkedQueue<Int>()
            .drainTo(sink::add)
        assertEquals(emptyList<Int>(), sink)
    }

    @Test
    fun `drains all, in order`() {
        val sink = mutableListOf<Int>()
        ConcurrentLinkedQueue(listOf(1, 2, 3))
            .drainTo(sink::add)
        assertEquals(listOf(1, 2, 3), sink)
    }
}
