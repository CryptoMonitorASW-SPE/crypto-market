@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure

import CryptoDetails
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import it.unibo.domain.CryptoChartData
import it.unibo.domain.CryptoRepository
import it.unibo.domain.CryptoSerializable
import it.unibo.domain.Currency
import it.unibo.domain.DataPoint
import it.unibo.infrastructure.metrics.ApiCallTracker
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object Config {
    private val logger: Logger = LoggerFactory.getLogger(Config::class.java)
    private val dotenv: Dotenv? =
        try {
            Dotenv.configure().directory("../../").load()
        } catch (e: DotenvException) {
            logger.error("Failed to load .env file: ${e.message}")
            null
        }

    // Retrieve the API key from environment variables
    val API_KEY: String? by lazy { System.getenv("COINGECKO_API_KEY") ?: dotenv?.get("COINGECKO_API_KEY") }
}

class CryptoRepositoryImpl(
    private val logger: Logger,
) : CryptoRepository {
    companion object {
        const val REQUEST_TIMEOUT_MILLIS = 10_000L
        const val CONNECT_TIMEOUT_MILLIS = 5_000L
        const val SOCKET_TIMEOUT_MILLIS = 5_000L
    }

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }
        }

//  private val ids = "bitcoin,ethereum,ripple,polkadot,solana,litecoin,cardano,doge"
    private val ids = "bitcoin,ethereum"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchCoinMarkets(currency: Currency): List<CryptoSerializable>? {
        if (Config.API_KEY == null) {
            logger.error("API key not found. Please set COINGECKO_API_KEY environment variable.")
            return null
        }
        val urlMarkets = "https://api.coingecko.com/api/v3/coins/markets"

        ApiCallTracker.recordApiCall() // Record the API call
        return try {
            val response: HttpResponse =
                client.get(urlMarkets) {
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
        } catch (e: ClientRequestException) {
            // 4xx responses
            logger.error("Client error: ${e.response.status} - ${e.message}")
            null
        } catch (e: ServerResponseException) {
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

    override suspend fun fetchCoinChartData(
        coinId: String,
        currency: Currency,
        days: Int,
    ): CryptoChartData? {
        if (Config.API_KEY == null) {
            logger.error("API key not found. Please set COINGECKO_API_KEY environment variable.")
            return null
        }
        val url = "https://api.coingecko.com/api/v3/coins/$coinId/ohlc"
        val cryptoChartData: CryptoChartData? =
            try {
                val response: HttpResponse =
                    client.get(url) {
                        parameter("vs_currency", currency.code)
                        parameter("days", days)
                        parameter("precision", 2)
                        header("accept", "application/json")
                        header("x-cg-demo-api-key", Config.API_KEY)
                    }
                logger.info("API request status: ${response.status}")
                if (response.status == HttpStatusCode.OK) {
                    val responseBody: String = response.bodyAsText()
                    val datapoints: List<DataPoint> = json.decodeFromString(responseBody)

                    CryptoChartData(
                        coinId = coinId,
                        currency = currency,
                        timespan = days,
                        dataPoints = datapoints,
                        timestamp = System.currentTimeMillis(),
                    )
                } else {
                    null
                }
            } catch (e: ClientRequestException) {
                // 4xx responses
                logger.error("Client error: ${e.response.status} - ${e.message}")
                null
            } catch (e: ServerResponseException) {
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
        return cryptoChartData
    }

    override suspend fun fetchCoinDetails(coinId: String): CryptoDetails? {
        if (Config.API_KEY == null) {
            logger.error("API key not found. Please set COINGECKO_API_KEY environment variable.")
            return null
        }
        val url =
            "https://api.coingecko.com/api/v3/coins/$coinId?localization=false&tickers=false" +
                "&market_data=true&community_data=true&developer_data=true&sparkline=false"
        val cryptoDetails: CryptoDetails? =
            try {
                val response: HttpResponse =
                    client.get(url) {
                        header("accept", "application/json")
                        header("x-cg-demo-api-key", Config.API_KEY)
                    }
                logger.info("API request status: ${response.status}")
                if (response.status == HttpStatusCode.OK) {
                    val responseBody: String = response.bodyAsText()
                    logger.info("Response body: $responseBody")
                    val cryptoDetails: CryptoDetails = json.decodeFromString(responseBody)
                    cryptoDetails
                } else {
                    null
                }
            } catch (e: ClientRequestException) {
                // 4xx responses
                logger.error("Client error: ${e.response.status} - ${e.message}")
                null
            } catch (e: ServerResponseException) {
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
        return cryptoDetails
    }

    override fun killClient() {
        client.close()
    }
}
