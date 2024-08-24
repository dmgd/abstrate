package dev.abstrate.logging

import dev.abstrate.kotlin.drainTo
import dev.abstrate.logging.LogLevel.Debug
import dev.abstrate.logging.LogLevel.Error
import dev.abstrate.logging.LogLevel.Info
import dev.abstrate.logging.LogLevel.None
import dev.abstrate.logging.LogLevel.Trace
import dev.abstrate.logging.LogLevel.Warning
import org.slf4j.ILoggerFactory
import org.slf4j.Marker
import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.event.Level
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter.arrayFormat
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

fun recordUnstructuredLogsTo(
    defaultLogLevel: LogLevel = Warning,
    logLevels: Map<String, LogLevel> = emptyMap(),
    record: (UnstructuredLog) -> Unit,
) {
    JavaUtilLoggingBridge.install()
    synchronized(deferred) {
        if (configuration != null) {
            error("Can't configure unstructured logging more than once")
        }
        configuration = UnstructuredLoggingConfiguration(record, logLevels, defaultLogLevel)
        deferred.drainTo {
            it.configure(configuration!!)
        }
    }
    JavaUtilLoggingBridge.setLogLevels(defaultLogLevel, logLevels)
}

enum class LogLevel {
    Trace, Debug, Info, Warning, Error, None
}

data class UnstructuredLog(
    val logger: String,
    val level: Level,
    val message: String,
    val throwable: Throwable?,
    /**
     * The log event's mapped diagnostic context (e.g. see [logback's docs](https://logback.qos.ch/manual/mdc.html)).
     *
     * Should be `null` or non-empty, to keep pointless `,mdc:{}` out of default json serialised form.
     * Some json serialisers may default to `,mdc:null` but it's reasonable to default to excluding keys with null values when the schema is known.
     * (It's _not_ reasonable to exclude empty maps except on a field by field basis, and that requires per-serialiser configuration which we're not going to do here.)
     */
    val mdc: Map<String, String>?,
    val marker: Marker?,
    val bufferedAt: Instant? = null,
) : Event

internal class UnstructuredLoggingConfiguration(
    val record: (UnstructuredLog) -> Unit,
    private val logLevelOverrides: Map<String, LogLevel>,
    private val defaultLogLevel: LogLevel,
) {
    fun logLevel(name: String): LogLevel =
        loggerNameHierarchy(name)
            .mapNotNull { logLevelOverrides[it] }
            .firstOrNull()
            ?: defaultLogLevel
}

internal fun loggerNameHierarchy(name: String) =
    generateSequence(name) { last ->
        last.substringBeforeLast('.')
            .takeIf { it != last }
    }

@Volatile
private var configuration: UnstructuredLoggingConfiguration? = null
private val deferred = ConcurrentLinkedQueue<DeferredSlf4jLogger>()

internal fun resetUnstructuredLoggingForTesting() {
    configuration = null
    deferred.clear()

    org.apache.commons.logging.LogFactory::class.java.getDeclaredField("logFactory")
        .apply {
            setAccessible(true)
            set(null, org.apache.commons.logging.impl.SLF4JLogFactory())
        }

    Class.forName("org.apache.log4j.Log4jLoggerFactory")
        .getDeclaredField("log4jLoggers")
        .apply {
            setAccessible(true)
            (get(null) as MutableMap<*, *>).clear()
        }

    java.util.logging.LogManager.getLogManager().reset()

    SLF4JBridgeHandler.install()
    java.util.logging.Logger.getLogger("").level = java.util.logging.Level.ALL
}

internal object JavaUtilLoggingBridge {
    init {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        // because of the way the bridge works, if this is set higher than the logged message's level, java.util.logging will never call into slf4j
        // the correct log level will be propagated back into java.util.logging by `recordUnstructuredLogsTo(..)` once we know what our logging levels are
        java.util.logging.Logger.getLogger("").level = java.util.logging.Level.ALL
    }

    // we need to keep a reference to the JUL loggers that we set levels for (see http://jira.qos.ch/browse//LOGBACK-404)
    private var julLoggersWithOverriddenSettings: List<java.util.logging.Logger>? = null

    fun install() {
        // everything we need to do was already done in `init` (so that we only do it once.) This method just makes it clear why we're referencing `JavaUtilLoggingBridge`
    }

    fun setLogLevels(defaultLogLevel: LogLevel, logLevels: Map<String, LogLevel>) {
        java.util.logging.Logger.getLogger("").level = defaultLogLevel.toJavaUtilLogging()
        julLoggersWithOverriddenSettings =
            logLevels.entries
                // set children (e.g. a.b.c) before parents (e.g. a.b) to avoid a race, e.g. between setting a.b to ERROR before a.b.c is set to TRACE and a TRACE message for a.b.c being filtered out before it's set
                .sortedByDescending { it.key }
                .map { (logger, level) ->
                    java.util.logging.Logger.getLogger(logger).also {
                        it.level = level.toJavaUtilLogging()
                    }
                }
    }
}

private fun LogLevel.toJavaUtilLogging() =
    when (this) {
        Trace -> java.util.logging.Level.FINEST
        Debug -> java.util.logging.Level.FINE
        Info -> java.util.logging.Level.INFO
        Warning -> java.util.logging.Level.WARNING
        Error -> java.util.logging.Level.SEVERE
        None -> java.util.logging.Level.OFF
    }

private fun unstructuredLog(logger: String, mdcAdapter: MDCAdapter, level: Level, marker: Marker?, messagePattern: String, arguments: Array<out Any>?, throwable: Throwable?) =
    UnstructuredLog(
        logger,
        level,
        arrayFormat(messagePattern, arguments, null).message,
        throwable,
        mdcAdapter.copyOfContextMap?.takeIf { it.isNotEmpty() },
        marker
    )

@Suppress("unused") // referenced in META-INF/services/org.slf4j.spi.SLF4JServiceProvider
internal class Slf4jServiceProvider : SLF4JServiceProvider {
    init {
        JavaUtilLoggingBridge.install()
    }
    private val mdcAdapter = BasicMDCAdapter()
    override fun getLoggerFactory() = Slf4jLoggerFactory(mdcAdapter)
    override fun getMarkerFactory() = BasicMarkerFactory()
    override fun getMDCAdapter() = mdcAdapter
    override fun getRequestedApiVersion(): String = SLF4JServiceProvider::class.java.getPackage().implementationVersion
    override fun initialize() {}
}

internal class Slf4jLoggerFactory(private val mdcAdapter: BasicMDCAdapter) : ILoggerFactory {
    override fun getLogger(name: String) =
        configuration?.let {
            Slf4jLogger(name, mdcAdapter, it)
        } ?: synchronized(deferred) {
            val configuration = configuration
            if (configuration == null) {
                DeferredSlf4jLogger(name, mdcAdapter)
                    .also {
                        deferred += it
                    }
            } else {
                Slf4jLogger(name, mdcAdapter, configuration)
            }
        }
}

internal class Slf4jLogger(name: String, private val mdcAdapter: MDCAdapter, private val configuration: UnstructuredLoggingConfiguration) : LegacyAbstractLogger() {
    private val logLevel = configuration.logLevel(name)
    init {
        this.name = name
    }
    override fun isTraceEnabled() = logLevel <= Trace
    override fun isDebugEnabled() = logLevel <= Debug
    override fun isInfoEnabled() = logLevel <= Info
    override fun isWarnEnabled() = logLevel <= Warning
    override fun isErrorEnabled() = logLevel <= Error
    override fun getFullyQualifiedCallerName() = null

    override fun handleNormalizedLoggingCall(level: Level, marker: Marker?, messagePattern: String, arguments: Array<out Any>?, throwable: Throwable?) {
        configuration.record(unstructuredLog(name, mdcAdapter, level, marker, messagePattern, arguments, throwable))
    }

    fun record(unstructuredLog: UnstructuredLog) {
        configuration.record(unstructuredLog)
    }

    fun recordIfLevelIsEnabled(unstructuredLog: UnstructuredLog) {
        val level =
            when (unstructuredLog.level) {
                Level.ERROR -> Error
                Level.WARN -> Warning
                Level.INFO -> Info
                Level.DEBUG -> Debug
                Level.TRACE -> Trace
            }
        if (logLevel <= level) {
            configuration.record(unstructuredLog)
        }
    }
}

internal class DeferredSlf4jLogger(name: String, private val mdcAdapter: MDCAdapter) : LegacyAbstractLogger() {
    init {
        this.name = name
    }
    private val buffer = ConcurrentLinkedQueue<UnstructuredLog>()
    @Volatile
    private var configured: Slf4jLogger? = null
    override fun isTraceEnabled() = configured?.isTraceEnabled ?: true
    override fun isDebugEnabled() = configured?.isDebugEnabled ?: true
    override fun isInfoEnabled() = configured?.isInfoEnabled ?: true
    override fun isWarnEnabled() = configured?.isWarnEnabled ?: true
    override fun isErrorEnabled() = configured?.isErrorEnabled ?: true
    override fun getFullyQualifiedCallerName() = null

    override fun handleNormalizedLoggingCall(level: Level, marker: Marker?, messagePattern: String, arguments: Array<out Any>?, throwable: Throwable?) {
        val log = unstructuredLog(name, mdcAdapter, level, marker, messagePattern, arguments, throwable)
        val notConfiguredYet = configured?.record(log) == null
        if (notConfiguredYet) {
            buffer += log.copy(bufferedAt = Instant.now())
            // it's possible that `configured` was set after the check we made above, but that `configure(..)` was already called, so the buffer won't be drained again
            drainBufferIfConfigured()
        }
    }

    fun configure(configuration: UnstructuredLoggingConfiguration) {
        configured = Slf4jLogger(name, mdcAdapter, configuration)
        drainBufferIfConfigured()
    }

    private fun drainBufferIfConfigured() {
        configured?.also {
            // only allow one thread to drain at a time (multiple threads would each take events off the queue in order, but calls to `configuration.record(..)` could be re-ordered)
            synchronized(buffer) {
                buffer.drainTo(it::recordIfLevelIsEnabled)
            }
        }
    }
}
