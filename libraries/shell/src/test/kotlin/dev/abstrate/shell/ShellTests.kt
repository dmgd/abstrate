package dev.abstrate.shell

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

interface ShellContract {

    val shell: Shell

    @Test
    fun `a successful command execution returns standard output`() {
        assertEquals(
            "1\n2\n3\n4\n",
            shell.execute(listOf("bash", "-c", "for i in {1..4}; do echo \$i; done")),
        )
    }

    @Test
    fun `a failing command execution throws ShellExecutionFailed`() {
        val failure =
            assertThrows<ShellExecutionFailed> {
                shell.execute(listOf("bash", "-c", "( echo \"This is stdout\"; echo \"This is stderr\" >&2; exit 42 )"))
            }
        // can't compare commands, since we might have wrapped them
        assertEquals(
            ShellExecutionFailed(exitCode = 42, stdout = "This is stdout\n", stderr = "This is stderr\n", command = emptyList()),
            failure.copy(command = emptyList()),
        )
    }

    @Test
    fun `testing execution of a successful command returns true`() {
        assertTrue(
            shell.test("true"),
        )
    }

    @Test
    fun `testing execution of a failed command returns false`() {
        assertFalse(
            shell.test("false"),
        )
    }
}

class TransientLocalShellTests : ShellContract {

    override val shell = TransientLocalShell
}
