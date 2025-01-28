package it.unibo.application

import it.unibo.domain.Crypto
import it.unibo.domain.CryptoRepository
import it.unibo.domain.CryptoSerializable
import it.unibo.domain.Currency
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.EventPayload
import it.unibo.infrastructure.adapter.EventType
import org.slf4j.Logger
import java.time.LocalDateTime

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
        val cryptoList = mutableListOf<Crypto>()

        val data: List<CryptoSerializable>? = repository.fetchCoinMarkets(currency)
        if (data != null) {
            logger.info("Retrieved ${data.size} coins in ${currency.code} at ${LocalDateTime.now()}")
            data.forEach { cryptoSerializable ->
                val crypto = mapToCrypto(cryptoSerializable)
                cryptoList.add(crypto)
            }
        } else {
            logger.warn("Failed to retrieve data for ${currency.code} at ${LocalDateTime.now()}")
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

    private fun mapToCrypto(crypto: CryptoSerializable): Crypto =
        Crypto(
            id = crypto.id,
            symbol = crypto.symbol,
            name = crypto.name,
            image = crypto.image!!,
            price = crypto.currentPrice,
            marketCap = crypto.marketCap?.toDouble(),
            marketCapRank = crypto.marketCapRank,
            fullyDilutedValuation = crypto.fullyDilutedValuation?.toDouble(),
            totalVolume = crypto.totalVolume?.toDouble(),
            high24h = crypto.high24h,
            low24h = crypto.low24h,
            priceChange24h = crypto.priceChange24h,
            priceChangePercentage24h = crypto.priceChangePercentage24h,
            marketCapChange24h = crypto.marketCapChange24h,
            marketCapChangePercentage24h = crypto.marketCapChangePercentage24h,
            circulatingSupply = crypto.circulatingSupply,
            totalSupply = crypto.totalSupply,
            maxSupply = crypto.maxSupply,
            ath = crypto.ath,
            athChangePercentage = crypto.athChangePercentage,
            athDate = crypto.athDate,
            atl = crypto.atl,
            atlChangePercentage = crypto.atlChangePercentage,
            atlDate = crypto.atlDate,
            lastUpdated = crypto.lastUpdated,
        )
}
