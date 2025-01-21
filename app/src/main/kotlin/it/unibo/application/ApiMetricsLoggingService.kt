package it.unibo.application

import it.unibo.infrastructure.metrics.ApiCallTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.slf4j.Logger
import java.time.LocalDateTime

class ApiMetricsLoggingService(
    private val logger: Logger,
) {
    companion object {
        const val LOGGING_INTERVAL_MILLIS = 60 * 1000L // One Minute
        const val LOGGING_INTERVAL_SECONDS = LOGGING_INTERVAL_MILLIS / 1000
    }

    private var isActive = false

    suspend fun startLogging() {
        isActive = true
        while (isActive) {
            delay(LOGGING_INTERVAL_MILLIS)
            val callsInInterval = ApiCallTracker.getAndResetCallsInInterval()
            val totalCalls = ApiCallTracker.getTotalCalls()
            if (callsInInterval > 0) {
                logger.info(
                    "API Calls - Total: $totalCalls, Last $LOGGING_INTERVAL_SECONDS " +
                        "seconds: $callsInInterval at ${LocalDateTime.now()}",
                )
            }
        }
    }

    fun stopLogging() {
        isActive = false
        logger.info("Stopping API metrics logging...")
    }
}
