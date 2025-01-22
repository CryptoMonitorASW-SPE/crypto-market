@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.unibo.domain.CryptoRepository
import it.unibo.domain.CryptoSerializable
import it.unibo.domain.Currency
import it.unibo.infrastructure.metrics.ApiCallTracker
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.time.LocalDateTime

object Config {
    private val dotenv: Dotenv? =
        try {
            Dotenv.configure().directory("../../").load()
        } catch (e: DotenvException) {
            null
        }

    // Retrieve the API key from environment variables
    val API_KEY: String? by lazy { System.getenv("COINGECKO_API_KEY") ?: dotenv?.get("COINGECKO_API_KEY") }
}

class CryptoRepositoryImpl(
    private val client: HttpClient,
    private val logger: Logger,
) : CryptoRepository {
    private val url = "https://api.coingecko.com/api/v3/coins/markets"

//  private val ids = "bitcoin,ethereum,ripple,polkadot,solana,litecoin,cardano,doge"
    private val ids = "bitcoin,ethereum"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchCoinMarkets(currency: Currency): List<CryptoSerializable>? {
        if (Config.API_KEY == null) {
            logger.error("API key not found. Please set COINGECKO_API_KEY environment variable.")
            return null
        }
        ApiCallTracker.recordApiCall() // Record the API call
        return try {
            val response: HttpResponse =
                client.get(url) {
                    parameter("vs_currency", currency.code)
                    parameter("ids", ids)
                    header("accept", "application/json")
                    header("x-cg-demo-api-key", Config.API_KEY)
                }
            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val cryptos: List<CryptoSerializable> = json.decodeFromString(responseBody)
                cryptos
            } else {
                logger.warn("API request failed with status ${response.status} at ${LocalDateTime.now()}")
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
        } catch (e: java.io.IOException) {
            // Network issues
            logger.error("Network error: ${e.localizedMessage}")
            null
        } catch (e: kotlinx.serialization.SerializationException) {
            // JSON parsing errors
            logger.error("Serialization error: ${e.localizedMessage}")
            null
        }
    }
}
