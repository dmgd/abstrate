package dev.abstrate.shell

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

interface Shell {

    fun execute(command: List<String>): Result<String, ShellExecutionFailed>
    fun test(command: List<String>): Boolean
}

fun Shell.execute(command: String) =
    execute(command.split(" "))

fun Shell.test(command: String) =
    test(command.split(" "))

data class ShellExecutionFailed(
    val command: List<String>,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) : RuntimeException()

/**
 * A shell that executes each command as a new local process.
 */
data object TransientLocalShell : Shell {

    override fun execute(command: List<String>): Result<String, ShellExecutionFailed> {
        val process = start(command)
        if (process.waitFor() != 0) {
            return Failure(
                ShellExecutionFailed(
                    command = command,
                    exitCode = process.waitFor(),
                    stdout = process.inputReader().readText(),
                    stderr = process.errorReader().readText(),
                )
            )
        }
        return Success(process.inputReader().readText())
    }

    override fun test(command: List<String>) =
        start(command)
            .waitFor() == 0

    private fun start(command: List<String>) =
        ProcessBuilder(command)
            .start()
}
