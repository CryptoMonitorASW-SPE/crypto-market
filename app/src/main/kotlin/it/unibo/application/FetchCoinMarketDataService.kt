package it.unibo.application

import it.unibo.domain.Crypto
import it.unibo.domain.CryptoRepository
import it.unibo.domain.CryptoSerializable
import it.unibo.domain.Currency
import it.unibo.domain.CurrencyValue
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
    }

    suspend fun fetchAndProcessData(): List<Crypto> {
        val startTime = System.currentTimeMillis()
        val currencies = Currency.getAllCurrencies()
        val cryptoMap = mutableMapOf<String, Crypto>()

        currencies.forEach { currency ->
            processCurrencyData(currency, cryptoMap)
        }

        val combinedCryptos = cryptoMap.values.toList()
        logger.info(
            "Combined data for ${combinedCryptos.size} cryptos at ${LocalDateTime.now()} in" +
                    " ${System.currentTimeMillis() - startTime} ms",
        )
        eventDispatcher.publish(combinedCryptos)
        return combinedCryptos
    }

    private suspend fun processCurrencyData(
        currency: Currency,
        cryptoMap: MutableMap<String, Crypto>,
    ) {
        val data: List<CryptoSerializable>? = repository.fetchCoinMarkets(currency)
        if (data != null) {
            logger.info("Retrieved ${data.size} coins in ${currency.code} at ${LocalDateTime.now()}")
            data.forEach { crypto ->
                updateCryptoMap(crypto, currency, cryptoMap)
            }
        } else {
            logger.warn("Failed to retrieve data for ${currency.code} at ${LocalDateTime.now()}")
        }
    }

    private fun updateCryptoMap(
        crypto: CryptoSerializable,
        currency: Currency,
        cryptoMap: MutableMap<String, Crypto>,
    ) {
        val existing = cryptoMap[crypto.id]
        if (existing != null) {
            // Helper to update nullable CurrencyValue fields
            fun updateField(
                current: CurrencyValue?,
                newValue: Double?,
            ) = current?.copy(values = current.values + (currency to newValue))

            cryptoMap[crypto.id] =
                existing.copy(
                    prices = updateField(existing.prices, crypto.currentPrice),
                    marketCap = updateField(existing.marketCap, crypto.marketCap?.toDouble()),
                    fullyDilutedValuation =
                        updateField(
                            existing.fullyDilutedValuation,
                            crypto.fullyDilutedValuation?.toDouble(),
                        ),
                    totalVolume = updateField(existing.totalVolume, crypto.totalVolume?.toDouble()),
                    high24h = updateField(existing.high24h, crypto.high24h),
                    low24h = updateField(existing.low24h, crypto.low24h),
                    priceChange24h = updateField(existing.priceChange24h, crypto.priceChange24h),
                    marketCapChange24h = updateField(existing.marketCapChange24h, crypto.marketCapChange24h),
                    ath = updateField(existing.ath, crypto.ath),
                    atl = updateField(existing.atl, crypto.atl),
                )
        } else {
            // Helper to create new CurrencyValue with current currency's value
            fun createValue(value: Double?) =
                CurrencyValue(
                    Currency.getAllCurrencies().associateWith { curr ->
                        if (curr == currency) value else null
                    },
                )

            cryptoMap[crypto.id] =
                Crypto(
                    id = crypto.id,
                    symbol = crypto.symbol,
                    name = crypto.name,
                    image = crypto.image,
                    prices = createValue(crypto.currentPrice),
                    marketCap = createValue(crypto.marketCap?.toDouble()),
                    marketCapRank = crypto.marketCapRank,
                    fullyDilutedValuation = createValue(crypto.fullyDilutedValuation?.toDouble()),
                    totalVolume = createValue(crypto.totalVolume?.toDouble()),
                    high24h = createValue(crypto.high24h),
                    low24h = createValue(crypto.low24h),
                    priceChange24h = createValue(crypto.priceChange24h),
                    priceChangePercentage24h = crypto.priceChangePercentage24h,
                    marketCapChange24h = createValue(crypto.marketCapChange24h),
                    marketCapChangePercentage24h = crypto.marketCapChangePercentage24h,
                    circulatingSupply = crypto.circulatingSupply,
                    totalSupply = crypto.totalSupply,
                    maxSupply = crypto.maxSupply,
                    ath = createValue(crypto.ath),
                    athChangePercentage = crypto.athChangePercentage,
                    athDate = crypto.athDate,
                    atl = createValue(crypto.atl),
                    atlChangePercentage = crypto.atlChangePercentage,
                    atlDate = crypto.atlDate,
                    lastUpdated = crypto.lastUpdated,
                )
        }
    }
}

