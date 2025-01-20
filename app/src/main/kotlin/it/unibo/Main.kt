package it.unibo

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import it.unibo.application.FetchCoinMarketDataService
import it.unibo.domain.CoinGeckoRepository
import it.unibo.infrastructure.CoinGeckoRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("CoinGeckoApp")

    // Configure Ktor client
    val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true // Ignore fields we don't map
                    },
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }
        }

    // Initialize the repository
    val repository: CoinGeckoRepository = CoinGeckoRepositoryImpl(client, logger)

    // Initialize the service
    val fetchService = FetchCoinMarketDataService(repository, logger)

    // Create a SupervisorJob for structured concurrency
    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    var isActive = true

    // Launch the scheduled job
    scope.launch {
        while (isActive) {
            fetchService.fetchAndProcessData()
        }
    }

    // Handle graceful shutdown
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                isActive = false
                logger.info("Shutting down...")
                supervisor.cancelAndJoin()
                client.close()
                logger.info("Ktor client closed.")
            }
        },
    )

    // Keep the main thread alive
    runBlocking {
        supervisor.join()
    }
}
