package it.unibo.domain

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class CryptoUnificationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Unify Bitcoin data from USD and EUR JSON`() =
        runBlocking {
            // Load USD JSON
            val usdJsonString = loadResource("bitcoin_usd.json")
            val bitcoinUsd: CryptoSerializable = json.decodeFromString(usdJsonString)

            // Load EUR JSON
            val eurJsonString = loadResource("bitcoin_eur.json")
            val bitcoinEur: CryptoSerializable = json.decodeFromString(eurJsonString)

            // Initialize empty crypto map
            val cryptoMap = mutableMapOf<String, Crypto>()

            // Process USD data
            processCurrencyDataTest(bitcoinUsd, Currency.USD, cryptoMap)

            // Process EUR data
            processCurrencyDataTest(bitcoinEur, Currency.EUR, cryptoMap)

            // Verify that cryptoMap has one entry for bitcoin
            assertEquals(1, cryptoMap.size, "Crypto map should contain exactly one entry for Bitcoin.")
            val bitcoin = cryptoMap["bitcoin"] ?: fail("Bitcoin not found in cryptoMap")

            // Verify the USD price
            assertNotNull(bitcoin.prices.usd, "USD price should not be null.")
            assertEquals(102775.0, bitcoin.prices.usd, "USD price does not match.")

            // Verify the EUR price
            assertNotNull(bitcoin.prices.eur, "EUR price should not be null.")
            assertEquals(94553.0, bitcoin.prices.eur, "EUR price does not match.")

            // Verify other fields if necessary
            assertEquals("bitcoin", bitcoin.id, "Crypto ID does not match.")
            assertEquals("btc", bitcoin.symbol, "Crypto symbol does not match.")
            assertEquals("Bitcoin", bitcoin.name, "Crypto name does not match.")
            assertEquals(
                "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400",
                bitcoin.image,
                "Crypto image URL does not match.",
            )
            // Add more assertions as needed for other fields
        }

    /**
     * Helper function to load resource files from the test resources directory.
     */
    private fun loadResource(fileName: String): String =
        this::class.java.classLoader
            .getResource(fileName)
            ?.readText() ?: throw IllegalArgumentException("Resource not found: $fileName")

    /**
     * Test-specific implementation of processCurrencyData.
     * This function mimics the behavior of the original processCurrencyData function.
     */
    private fun processCurrencyDataTest(
        crypto: CryptoSerializable,
        currency: Currency,
        cryptoMap: MutableMap<String, Crypto>,
    ) {
        if (crypto != null) {
            // Update the crypto map with the new data
            updateCryptoMap(crypto, currency, cryptoMap)
        } else {
            // Handle the case where data retrieval failed
            // In this test, we assume data is always present
            fail("Crypto data is null for currency: ${currency.code}")
        }
    }

    /**
     * Mimics the original updateCryptoMap function.
     * Make sure this function is accessible to the test (e.g., internal or public).
     */
    private fun updateCryptoMap(
        crypto: CryptoSerializable,
        currency: Currency,
        cryptoMap: MutableMap<String, Crypto>,
    ) {
        val existing = cryptoMap[crypto.id]
        if (existing != null) {
            val updatedPrice =
                existing.prices.copy(
                    usd = if (currency is Currency.USD) crypto.currentPrice else existing.prices.usd,
                    eur = if (currency is Currency.EUR) crypto.currentPrice else existing.prices.eur,
                )
            cryptoMap[crypto.id] = existing.copy(prices = updatedPrice)
        } else {
            val price =
                when (currency) {
                    is Currency.USD -> Price(usd = crypto.currentPrice, eur = null)
                    is Currency.EUR -> Price(usd = null, eur = crypto.currentPrice)
                    // Add other currencies if needed
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
}
