
package it.unibo.domain

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CryptoDeserializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Crypto deserialization from USD JSON`() {
        val jsonString = loadResource("bitcoin_usd.json")
        val crypto: CryptoSerializable = json.decodeFromString(jsonString)

        assertEquals("bitcoin", crypto.id)
        assertEquals("btc", crypto.symbol)
        assertEquals("Bitcoin", crypto.name)
        assertEquals("https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400", crypto.image)
        assertEquals(102775.0, crypto.currentPrice!!)
        assertEquals(2042960757968, crypto.marketCap!!)
        assertEquals(1, crypto.marketCapRank!!)
        assertEquals(2042962407751, crypto.fullyDilutedValuation!!)
        assertEquals(106133281044, crypto.totalVolume!!)
        assertEquals(107483.0, crypto.high24h!!)
        assertEquals(100142.0, crypto.low24h!!)
        assertEquals(-4707.98, crypto.priceChange24h!!)
        assertEquals(-4.38, crypto.priceChangePercentage24h!!)
        assertEquals(-58610573965.45, crypto.marketCapChange24h!!, 1e-2)
        assertEquals(-4.04, crypto.marketCapChangePercentage24h!!)
        assertEquals(19813137.0, crypto.circulatingSupply!!)
        assertEquals(19813159.0, crypto.totalSupply!!)
        assertEquals(21000000.0, crypto.maxSupply!!)
        assertEquals(108786.0, crypto.ath!!)
        assertEquals(-5.28, crypto.athChangePercentage!!)
        assertEquals("2025-01-20T09:11:54.494Z", crypto.athDate)
        assertEquals(67.81, crypto.atl!!)
        assertEquals(151856.46, crypto.atlChangePercentage!!)
        assertEquals("2013-07-06T00:00:00.000Z", crypto.atlDate)
        assertEquals("2025-01-21T15:06:01.613Z", crypto.lastUpdated)
    }

    @Test
    fun `Crypto deserialization from EUR JSON`() {
        val jsonString = loadResource("ethereum_eur.json")
        val crypto: CryptoSerializable = json.decodeFromString(jsonString)

        assertEquals("ethereum", crypto.id)
        assertEquals("eth", crypto.symbol)
        assertEquals("Ethereum", crypto.name)
        assertEquals("https://coin-images.coingecko.com/coins/images/279/large/ethereum.png?1696501628", crypto.image)
        assertEquals(3271.22, crypto.currentPrice!!)
        assertEquals(395481827089, crypto.marketCap!!)
        assertEquals(2, crypto.marketCapRank!!)
        assertEquals(395481827089, crypto.fullyDilutedValuation!!)
        assertEquals(41044458814, crypto.totalVolume!!)
        assertEquals(3381.35, crypto.high24h!!)
        assertEquals(3194.32, crypto.low24h!!)
        assertEquals(-78.47, crypto.priceChange24h!!)
        assertEquals(-2.34, crypto.priceChangePercentage24h!!)
        assertEquals(-7233006024.16, crypto.marketCapChange24h!!, 1e-2)
        assertEquals(-1.80, crypto.marketCapChangePercentage24h!!)
        assertEquals(120500685.1, crypto.circulatingSupply!!)
        assertEquals(120500685.1, crypto.totalSupply!!)
        assertEquals(4878.26, crypto.ath!!)
        assertEquals(-32.79, crypto.athChangePercentage!!)
        assertEquals("2021-11-10T14:24:19.604Z", crypto.athDate)
        assertEquals(0.432979, crypto.atl!!)
        assertEquals(757073.86, crypto.atlChangePercentage!!)
        assertEquals("2015-10-20T00:00:00.000Z", crypto.atlDate)
        assertEquals("2025-01-21T15:06:01.226Z", crypto.lastUpdated)
    }

    private fun loadResource(fileName: String): String =
        this::class.java.classLoader
            .getResource(fileName)
            ?.readText() ?: throw IllegalArgumentException("Resource not found: $fileName")
}
