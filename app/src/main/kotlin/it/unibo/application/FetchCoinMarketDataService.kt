package it.unibo.application

import it.unibo.domain.CoinGeckoRepository
import it.unibo.domain.CoinMarket
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import kotlinx.coroutines.delay
import org.slf4j.Logger
import java.time.LocalDateTime

class FetchCoinMarketDataService(
    private val repository: CoinGeckoRepository,
    private val logger: Logger,
    private val eventDispatcher: EventDispatcherAdapter
) {
    companion object {
        private const val DELAY_MINUTES = 5
        private const val MILLISECONDS_IN_A_MINUTE = (60 * 1000).toLong()
    }

    suspend fun fetchAndProcessData() {
        val startTime = System.currentTimeMillis()
        val data: List<CoinMarket>? = repository.fetchCoinMarkets()
        if (data != null) {
            // Log successful retrieval
            logger.info("Retrieved $data coins at ${LocalDateTime.now()}")

            data.forEach{
                eventDispatcher.publish(it)
            }

        } else {
            logger.warn("Failed to retrieve data at ${LocalDateTime.now()}")
        }

        // Calculate delay to run every 5 minutes
        val elapsedTime = System.currentTimeMillis() - startTime
        val delayTime = DELAY_MINUTES * MILLISECONDS_IN_A_MINUTE - elapsedTime
        if (delayTime > 0) {
            delay(delayTime)
        }
    }
}
