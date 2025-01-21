package it.unibo.infrastructure.metrics

import it.unibo.application.ApiMetricsLoggingService.Companion.LOGGING_INTERVAL_MILLIS
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

object ApiCallTracker {
    private val totalCalls = AtomicLong(0)
    private val callsInCurrentInterval = AtomicLong(0)
    private val mutex = Mutex()
    private var windowStart = Instant.now().toEpochMilli()

    /**
     * Records an API call.
     */
    fun recordApiCall() {
        totalCalls.incrementAndGet()
        callsInCurrentInterval.incrementAndGet()
    }

    /**
     * Retrieves and resets the number of API calls in the current interval.
     */
    suspend fun getAndResetCallsInInterval(): Long =
        mutex.withLock {
            val now = Instant.now().toEpochMilli()
            if (now - windowStart >= LOGGING_INTERVAL_MILLIS) {
                val calls = callsInCurrentInterval.getAndSet(0)
                windowStart = now
                calls
            } else {
                0
            }
        }

    /**
     * Retrieves the total number of API calls.
     */
    fun getTotalCalls(): Long = totalCalls.get()
}
