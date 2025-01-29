package it.unibo.application

import it.unibo.domain.Crypto
import it.unibo.domain.CryptoRepository
import it.unibo.domain.Currency
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.EventPayload
import it.unibo.infrastructure.adapter.EventType
import org.slf4j.Logger

class FetchCoinMarketDataService(
    private val repository: CryptoRepository,
    private val logger: Logger,
    private val eventDispatcher: EventDispatcherAdapter,
) {
    companion object {
        const val DELAY_MINUTES = 1
    }

    suspend fun fetchAndProcessData(currency: Currency): List<Crypto> {
        val startTime = System.currentTimeMillis()
        val cryptoList: List<Crypto>? = repository.fetchCoinMarkets(currency)
        if (cryptoList == null) {
            logger.error("Failed to fetch data from CoinGecko API")
            return emptyList()
        }
        val endTime = System.currentTimeMillis()
        logger.info("Data processing completed in ${endTime - startTime}ms")

        val eventType =
            when (currency) {
                Currency.USD -> EventType.CRYPTO_UPDATE_USD
                Currency.EUR -> EventType.CRYPTO_UPDATE_EUR
            }
        eventDispatcher.publish(EventPayload(eventType = eventType, payload = cryptoList))
        return cryptoList
    }
}
