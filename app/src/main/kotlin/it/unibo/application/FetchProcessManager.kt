package it.unibo.application

import io.ktor.util.network.*
import it.unibo.domain.Currency
import it.unibo.infrastructure.adapter.EventPayload
import it.unibo.infrastructure.adapter.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class FetchProcessManager(
    private val fetchService: FetchCoinMarketDataService,
    private val scope: CoroutineScope,
) {
    private val logger = LoggerFactory.getLogger(FetchProcessManager::class.java)

    // Map to hold active jobs per currency
    private val fetchJobs = ConcurrentHashMap<Currency, Job>()

    // Map to hold latest data per currency
    private val latestData = ConcurrentHashMap<Currency, EventPayload>()

    // Check if a job is running for a specific currency
    fun isRunning(currency: Currency): Boolean = fetchJobs[currency]?.isActive ?: false

    // Start fetching data for a specific currency
    fun start(currency: Currency) {
        if (isRunning(currency)) return
        val job =
            scope.launch {
                while (isActive) {
                    try {
                        val data = fetchService.fetchAndProcessData(currency)
                        val eventType =
                            when (currency) {
                                Currency.USD -> EventType.CRYPTO_UPDATE_USD
                                Currency.EUR -> EventType.CRYPTO_UPDATE_EUR
                            }
                        latestData[currency] = EventPayload(eventType = eventType, payload = data)
                    } catch (e: IOException) {
                        logger.error("Failed to fetch data for $currency", e)
                    } catch (e: SerializationException) {
                        logger.error("Failed to parse data for $currency", e)
                    } catch (e: UnresolvedAddressException) {
                        logger.error("Failed to resolve address for $currency", e)
                    }
                    delay(FetchCoinMarketDataService.DELAY_MINUTES * MINUTES_TO_MS)
                }
            }
        fetchJobs[currency] = job
    }

    // Stop fetching data for a specific currency
    fun stop(currency: Currency) {
        fetchJobs[currency]?.cancel()
        fetchJobs.remove(currency)
        latestData.remove(currency)
    }

    // Get the latest data for a specific currency
    fun getLatestData(currency: Currency): EventPayload? = latestData[currency]

    // Stop all fetch jobs
    fun stopAll() {
        fetchJobs.values.forEach { it.cancel() }
        fetchJobs.clear()
        latestData.clear()
    }

    companion object {
        const val MINUTES_TO_MS = 60_000L
    }
}
