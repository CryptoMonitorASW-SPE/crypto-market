package it.unibo.application

import it.unibo.domain.CoinGeckoRepository
import it.unibo.domain.CoinMarket
import kotlinx.coroutines.delay
import org.slf4j.Logger
import java.time.LocalDateTime

class FetchCoinMarketDataService(
    private val repository: CoinGeckoRepository,
    private val logger: Logger,
) {
    suspend fun fetchAndProcessData() {
        val startTime = System.currentTimeMillis()
        val data: List<CoinMarket>? = repository.fetchCoinMarkets()
        if (data != null) {
            // Log successful retrieval
            logger.info("Retrieved $data coins at ${LocalDateTime.now()}")
        } else {
            logger.warn("Failed to retrieve data at ${LocalDateTime.now()}")
        }

        // Calculate delay to run every 5 minutes
        val elapsedTime = System.currentTimeMillis() - startTime
        val delayTime = 5 * 60 * 1000 - elapsedTime
        if (delayTime > 0) {
            delay(delayTime)
        }
    }
}
