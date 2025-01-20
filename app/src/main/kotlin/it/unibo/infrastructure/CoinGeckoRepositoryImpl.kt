// src/main/kotlin/infrastructure/repositories/CoinGeckoRepositoryImpl.kt

package it.unibo.infrastructure

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.unibo.domain.CoinGeckoRepository
import it.unibo.domain.CoinMarket
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.time.LocalDateTime

object Config {
    // Retrieve the API key from environment variables
    val API_KEY: String by lazy { System.getenv("COINGECKO_API_KEY") ?: "SECRETSECRET" }
}

class CoinGeckoRepositoryImpl(
    private val client: HttpClient,
    private val logger: Logger,
) : CoinGeckoRepository {
    private val url = "https://api.coingecko.com/api/v3/coins/markets"
    private val vsCurrency = "usd"
    private val ids = "bitcoin,ethereum,ripple,polkadot,solana,litecoin,cardano,doge"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchCoinMarkets(): List<CoinMarket>? =
        try {
            val response: HttpResponse =
                client.get(url) {
                    parameter("vs_currency", vsCurrency)
                    parameter("ids", ids)
                    header("accept", "application/json")
                    header("x-cg-demo-api-key", Config.API_KEY)
                }
            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val coinMarkets: List<CoinMarket> = json.decodeFromString(responseBody)
                logger.info("Successfully fetched and parsed data at ${LocalDateTime.now()}")
                coinMarkets
            } else {
                logger.error("Failed to fetch data: ${response.status}")
                null
            }
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            // 4xx responses
            logger.error("Client error: ${e.response.status} - ${e.message}")
            null
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            // 5xx responses
            logger.error("Server error: ${e.response.status} - ${e.message}")
            null
        } catch (e: Exception) {
            // Other errors (e.g., network issues)
            logger.error("Unexpected error: ${e.localizedMessage}")
            null
        }
}
