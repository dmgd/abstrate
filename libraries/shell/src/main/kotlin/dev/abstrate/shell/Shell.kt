package dev.abstrate.shell

interface Shell {

    fun execute(command: List<String>): String
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
) : Exception()

/**
 * A shell that executes each command as a new local process.
 */
data object TransientLocalShell : Shell {

    override fun execute(command: List<String>): String {
        val process = start(command)
        if (process.waitFor() != 0) {
            throw ShellExecutionFailed(
                command = command,
                exitCode = process.waitFor(),
                stdout = process.inputReader().readText(),
                stderr = process.errorReader().readText(),
            )
        }
        return process.inputReader().readText()
    }

    override fun test(command: List<String>) =
        start(command)
            .waitFor() == 0

    private fun start(command: List<String>) =
        ProcessBuilder(command)
            .start()
}
