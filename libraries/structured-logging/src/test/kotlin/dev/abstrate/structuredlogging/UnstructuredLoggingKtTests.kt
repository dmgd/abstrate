package dev.abstrate.structuredlogging

import dev.abstrate.structuredlogging.LogLevel.Error
import dev.abstrate.structuredlogging.LogLevel.Info
import dev.abstrate.structuredlogging.LogLevel.Trace
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.slf4j.Marker
import org.slf4j.event.Level
import java.time.Instant

// slf4j and java.util.logging use global config, so regardless of whether other tests execute in parallel or not, these
@Execution(ExecutionMode.SAME_THREAD)
class UnstructuredLoggingKtTests {

    private val logs = mutableListOf<UnstructuredLog>()

    @Suppress("SameParameterValue")
    private fun recordUnstructuredLogs(defaultLogLevel: LogLevel, logLevels: Map<String, LogLevel> = emptyMap()) {
        recordUnstructuredLogsTo(defaultLogLevel, logLevels) {
            logs += it
        }
    }

    // reset before + after so that the first test starts with a clean slate and the last test leaves one
    @BeforeEach
    @AfterEach
    fun init() {
        resetUnstructuredLoggingForTesting()
    }

    private fun unstructuredLog(logger: String, level: Level, message: String, throwable: Throwable? = null, mdc: Map<String, String>? = null, marker: Marker? = null, bufferedAt: Instant? = null) =
        UnstructuredLog(logger = logger, level = level, message = message, throwable = throwable, mdc = mdc?.takeIf { it.isNotEmpty() }, marker = marker, bufferedAt = bufferedAt)

    @Test
    fun `logger names form a dot-separated hierarchy`() {
        assertEquals(
            listOf("a"),
            loggerNameHierarchy("a").toList(),
        )
        assertEquals(
            listOf("a.bc.d", "a.bc", "a"),
            loggerNameHierarchy("a.bc.d").toList(),
        )
    }

    @Test
    fun `log levels are inherited`() {
        val configuration =
            UnstructuredLoggingConfiguration(
                {},
                logLevelOverrides = mapOf(
                    "a.b.c" to Error,
                    "a.b" to Trace,
                ),
                defaultLogLevel = Info,
            )

        fun assertLogLevel(logger: String, level: LogLevel) {
            assertEquals(level, configuration.logLevel(logger), "logger $logger")
        }
        assertLogLevel("a", Info)
        assertLogLevel("a.b", Trace)
        assertLogLevel("a.b.c", Error)
        assertLogLevel("a.b.c.d", Error)
        assertLogLevel("a.b.d", Trace)
    }

    @Test
    fun `slf4j logs are filtered as expected`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.slf4j.LoggerFactory.getLogger("a").debug("testing a trace")
        org.slf4j.LoggerFactory.getLogger("a").warn("testing a warning")
        org.slf4j.LoggerFactory.getLogger("a.b").trace("testing a.b trace")
        org.slf4j.LoggerFactory.getLogger("a.b").warn("testing a.b warning")
        org.slf4j.LoggerFactory.getLogger("a.b.c").trace("testing a.b.c trace")
        org.slf4j.LoggerFactory.getLogger("a.b.c").warn("testing a.b.c warning")
        org.slf4j.LoggerFactory.getLogger("a.b.c").error("testing a.b.c error")
        org.slf4j.LoggerFactory.getLogger("a.b.c.d").trace("testing a.b.c.d trace")
        org.slf4j.LoggerFactory.getLogger("a.b.c.d").error("testing a.b.c.d error")
        org.slf4j.LoggerFactory.getLogger("a.b.d").trace("testing a.b.d trace")
        org.slf4j.LoggerFactory.getLogger("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning"),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace"),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning"),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error"),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error"),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs,
        )
    }

    @Test
    fun `slf4j logs are buffered before unstructured logging was initialised`() {
        org.slf4j.LoggerFactory.getLogger("a").debug("testing a trace")
        org.slf4j.LoggerFactory.getLogger("a").warn("testing a warning")
        org.slf4j.LoggerFactory.getLogger("a.b").trace("testing a.b trace")
        org.slf4j.LoggerFactory.getLogger("a.b").warn("testing a.b warning")
        org.slf4j.LoggerFactory.getLogger("a.b.c").trace("testing a.b.c trace")
        org.slf4j.LoggerFactory.getLogger("a.b.c").warn("testing a.b.c warning")
        org.slf4j.LoggerFactory.getLogger("a.b.c").error("testing a.b.c error")
        org.slf4j.LoggerFactory.getLogger("a.b.c.d").trace("testing a.b.c.d trace")
        org.slf4j.LoggerFactory.getLogger("a.b.c.d").error("testing a.b.c.d error")
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.slf4j.LoggerFactory.getLogger("a.b.d").trace("testing a.b.d trace")
        org.slf4j.LoggerFactory.getLogger("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs.map { if (it.bufferedAt != null) it.copy(bufferedAt = Instant.EPOCH) else it },
        )
    }

    @Test
    fun `we capture slf4j markers, throwables, and MDC`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        val marker = org.slf4j.MarkerFactory.getMarker("something")
        val throwable = Exception("some failure")
        val mdc = mapOf("a" to "1", "b" to "2")
        org.slf4j.MDC.setContextMap(mdc)
        try {
            org.slf4j.LoggerFactory.getLogger("a").warn(marker, "testing", throwable)
        } finally {
            org.slf4j.MDC.clear()
        }
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing", throwable = throwable, mdc = mdc, marker = marker),
            ),
            logs,
        )
    }

    @Test
    fun `java util logging logs are filtered as expected`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        java.util.logging.Logger.getLogger("a").finest("testing a trace")
        java.util.logging.Logger.getLogger("a").warning("testing a warning")
        java.util.logging.Logger.getLogger("a.b").finest("testing a.b trace")
        java.util.logging.Logger.getLogger("a.b").warning("testing a.b warning")
        java.util.logging.Logger.getLogger("a.b.c").finest("testing a.b.c trace")
        java.util.logging.Logger.getLogger("a.b.c").warning("testing a.b.c warning")
        java.util.logging.Logger.getLogger("a.b.c").severe("testing a.b.c error")
        java.util.logging.Logger.getLogger("a.b.c.d").finest("testing a.b.c.d trace")
        java.util.logging.Logger.getLogger("a.b.c.d").severe("testing a.b.c.d error")
        java.util.logging.Logger.getLogger("a.b.d").finest("testing a.b.d trace")
        java.util.logging.Logger.getLogger("a.b.d").warning("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning"),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace"),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning"),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error"),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error"),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs,
        )
    }

    @Test
    fun `java util logging logs are buffered before unstructured logging was initialised`() {
        java.util.logging.Logger.getLogger("a").finest("testing a trace")
        java.util.logging.Logger.getLogger("a").warning("testing a warning")
        java.util.logging.Logger.getLogger("a.b").finest("testing a.b trace")
        java.util.logging.Logger.getLogger("a.b").warning("testing a.b warning")
        java.util.logging.Logger.getLogger("a.b.c").finest("testing a.b.c trace")
        java.util.logging.Logger.getLogger("a.b.c").warning("testing a.b.c warning")
        java.util.logging.Logger.getLogger("a.b.c").severe("testing a.b.c error")
        java.util.logging.Logger.getLogger("a.b.c.d").finest("testing a.b.c.d trace")
        java.util.logging.Logger.getLogger("a.b.c.d").severe("testing a.b.c.d error")
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        java.util.logging.Logger.getLogger("a.b.d").finest("testing a.b.d trace")
        java.util.logging.Logger.getLogger("a.b.d").warning("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs.map { if (it.bufferedAt != null) it.copy(bufferedAt = Instant.EPOCH) else it },
        )
    }

    @Test
    fun `log4j logs are filtered as expected`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.apache.log4j.Logger.getLogger("a").debug("testing a trace")
        org.apache.log4j.Logger.getLogger("a").warn("testing a warning")
        org.apache.log4j.Logger.getLogger("a.b").trace("testing a.b trace")
        org.apache.log4j.Logger.getLogger("a.b").warn("testing a.b warning")
        org.apache.log4j.Logger.getLogger("a.b.c").trace("testing a.b.c trace")
        org.apache.log4j.Logger.getLogger("a.b.c").warn("testing a.b.c warning")
        org.apache.log4j.Logger.getLogger("a.b.c").error("testing a.b.c error")
        org.apache.log4j.Logger.getLogger("a.b.c.d").trace("testing a.b.c.d trace")
        org.apache.log4j.Logger.getLogger("a.b.c.d").error("testing a.b.c.d error")
        org.apache.log4j.Logger.getLogger("a.b.d").trace("testing a.b.d trace")
        org.apache.log4j.Logger.getLogger("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning"),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace"),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning"),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error"),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error"),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs,
        )
    }

    @Test
    fun `log4j logs are buffered before unstructured logging was initialised`() {
        org.apache.log4j.Logger.getLogger("a").debug("testing a trace")
        org.apache.log4j.Logger.getLogger("a").warn("testing a warning")
        org.apache.log4j.Logger.getLogger("a.b").trace("testing a.b trace")
        org.apache.log4j.Logger.getLogger("a.b").warn("testing a.b warning")
        org.apache.log4j.Logger.getLogger("a.b.c").trace("testing a.b.c trace")
        org.apache.log4j.Logger.getLogger("a.b.c").warn("testing a.b.c warning")
        org.apache.log4j.Logger.getLogger("a.b.c").error("testing a.b.c error")
        org.apache.log4j.Logger.getLogger("a.b.c.d").trace("testing a.b.c.d trace")
        org.apache.log4j.Logger.getLogger("a.b.c.d").error("testing a.b.c.d error")
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.apache.log4j.Logger.getLogger("a.b.d").trace("testing a.b.d trace")
        org.apache.log4j.Logger.getLogger("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs.map { if (it.bufferedAt != null) it.copy(bufferedAt = Instant.EPOCH) else it },
        )
    }

    @Test
    fun `we capture log4j throwables and MDC`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        val throwable = Exception("some failure")
        val mdc = mapOf("a" to "1", "b" to "2")
        mdc.forEach { (k, v) ->
            org.apache.log4j.MDC.put(k, v)
        }
        try {
            org.apache.log4j.Logger.getLogger("a").warn("testing", throwable)
        } finally {
            org.apache.log4j.MDC.clear()
        }
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing", throwable = throwable, mdc = mdc),
            ),
            logs,
        )
    }

    @Test
    fun `commons logging logs are filtered as expected`() {
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.apache.commons.logging.LogFactory.getLog("a").debug("testing a trace")
        org.apache.commons.logging.LogFactory.getLog("a").warn("testing a warning")
        org.apache.commons.logging.LogFactory.getLog("a.b").trace("testing a.b trace")
        org.apache.commons.logging.LogFactory.getLog("a.b").warn("testing a.b warning")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").trace("testing a.b.c trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").warn("testing a.b.c warning")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").error("testing a.b.c error")
        org.apache.commons.logging.LogFactory.getLog("a.b.c.d").trace("testing a.b.c.d trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.c.d").error("testing a.b.c.d error")
        org.apache.commons.logging.LogFactory.getLog("a.b.d").trace("testing a.b.d trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning"),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace"),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning"),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error"),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error"),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs,
        )
    }

    @Test
    fun `commons logging logs are buffered before unstructured logging was initialised`() {
        org.apache.commons.logging.LogFactory.getLog("a").warn("testing a warning")
        org.apache.commons.logging.LogFactory.getLog("a").debug("testing a trace")
        org.apache.commons.logging.LogFactory.getLog("a.b").trace("testing a.b trace")
        org.apache.commons.logging.LogFactory.getLog("a.b").warn("testing a.b warning")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").trace("testing a.b.c trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").warn("testing a.b.c warning")
        org.apache.commons.logging.LogFactory.getLog("a.b.c").error("testing a.b.c error")
        org.apache.commons.logging.LogFactory.getLog("a.b.c.d").trace("testing a.b.c.d trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.c.d").error("testing a.b.c.d error")
        recordUnstructuredLogs(
            defaultLogLevel = Info,
            logLevels = mapOf(
                "a.b.c" to Error,
                "a.b" to Trace,
            ),
        )
        org.apache.commons.logging.LogFactory.getLog("a.b.d").trace("testing a.b.d trace")
        org.apache.commons.logging.LogFactory.getLog("a.b.d").warn("testing a.b.d warning")
        assertEquals(
            listOf(
                unstructuredLog(logger = "a", level = Level.WARN, message = "testing a warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.TRACE, message = "testing a.b trace", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b", level = Level.WARN, message = "testing a.b warning", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c", level = Level.ERROR, message = "testing a.b.c error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.c.d", level = Level.ERROR, message = "testing a.b.c.d error", bufferedAt = Instant.EPOCH),
                unstructuredLog(logger = "a.b.d", level = Level.TRACE, message = "testing a.b.d trace"),
                unstructuredLog(logger = "a.b.d", level = Level.WARN, message = "testing a.b.d warning"),
            ),
            logs.map { if (it.bufferedAt != null) it.copy(bufferedAt = Instant.EPOCH) else it },
        )
    }
}
