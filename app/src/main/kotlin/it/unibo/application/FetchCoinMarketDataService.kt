package it.unibo.application

import it.unibo.domain.Crypto
import it.unibo.domain.CryptoRepository
import it.unibo.domain.CryptoSerializable
import it.unibo.domain.Currency
import it.unibo.domain.Price
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import org.slf4j.Logger
import java.time.LocalDateTime

class FetchCoinMarketDataService(
    private val repository: CryptoRepository,
    private val logger: Logger,
    private val eventDispatcher: EventDispatcherAdapter,
) {
    companion object {
        const val DELAY_MINUTES = 5
        const val MILLISECONDS_IN_A_MINUTE = (60 * 1000).toLong()
    }

    suspend fun fetchAndProcessData() {
        val startTime = System.currentTimeMillis()
        val currencies = Currency.getAllCurrencies()

        val cryptoMap = mutableMapOf<String, Crypto>()

        currencies.forEach {
            val data: List<CryptoSerializable>? = repository.fetchCoinMarkets(it)
            if (data != null) {
                logger.info("Retrieved ${data.size} coins in ${it.code} at ${LocalDateTime.now()}")
                data.forEach { crypto ->
                    val existing = cryptoMap[crypto.id]
                    if (existing != null) {
                        // Update the prices
                        val updatedPrice =
                            existing.prices.copy(
                                usd = if (it is Currency.USD) crypto.currentPrice else existing.prices.usd,
                                eur = if (it is Currency.EUR) crypto.currentPrice else existing.prices.eur,
                            )
                        cryptoMap[crypto.id] = existing.copy(prices = updatedPrice)
                    } else {
                        // Initialize the CryptoDomain object
                        val price =
                            when (it) {
                                is Currency.USD -> Price(usd = crypto.currentPrice, eur = null)
                                is Currency.EUR -> Price(usd = null, eur = crypto.currentPrice)
                            }

                        cryptoMap[crypto.id] =
                            Crypto(
                                id = crypto.id,
                                symbol = crypto.symbol,
                                name = crypto.name,
                                image = crypto.image,
                                prices = price,
                                marketCap = crypto.marketCap,
                                marketCapRank = crypto.marketCapRank,
                                fullyDilutedValuation = crypto.fullyDilutedValuation,
                                totalVolume = crypto.totalVolume,
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
                }
                // logger.info("Successfully fetched and processed data for ${it.code} at ${LocalDateTime.now()}")
            } else {
                logger.warn("Failed to retrieve data for ${it.code} at ${LocalDateTime.now()}")
            }
        }

        // At this point, cryptoMap contains combined data for each crypto
        val combinedCryptos = cryptoMap.values.toList()
        logger.info(
            "Combined data for ${combinedCryptos.size} cryptos at ${LocalDateTime.now()} in ${System.currentTimeMillis() - startTime} ms",
        )
        logger.info("$combinedCryptos")
        eventDispatcher.publish(combinedCryptos)
    }
}
