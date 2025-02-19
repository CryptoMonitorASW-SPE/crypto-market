@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.application

import io.ktor.util.network.*
import it.unibo.domain.Currency
import it.unibo.domain.EventPayload
import it.unibo.domain.EventType
import it.unibo.domain.ports.FetchCoinMarketData
import it.unibo.domain.ports.FetchProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class FetchProcessManager(
    private val fetchService: FetchCoinMarketData,
    private val scope: CoroutineScope,
) : FetchProcess {
    private val logger = LoggerFactory.getLogger(FetchProcessManager::class.java)

    private val fetchJobs = ConcurrentHashMap<Currency, Job>()

    private val latestData = ConcurrentHashMap<Currency, EventPayload>()

    override fun isRunning(currency: Currency): Boolean = fetchJobs[currency]?.isActive ?: false

    override fun start(currency: Currency) {
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

    override fun stop(currency: Currency) {
        fetchJobs[currency]?.cancel()
        fetchJobs.remove(currency)
        latestData.remove(currency)
    }

    override fun getLatestData(currency: Currency): EventPayload? = latestData[currency]

    override fun stopAll() {
        fetchJobs.values.forEach { it.cancel() }
        fetchJobs.clear()
        latestData.clear()
    }

    companion object {
        const val MINUTES_TO_MS = 60_000L
    }
}
