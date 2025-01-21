package it.unibo

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import it.unibo.application.ApiMetricsLoggingService
import it.unibo.application.FetchCoinMarketDataService
import it.unibo.domain.CryptoRepository
import it.unibo.infrastructure.CryptoRepositoryImpl
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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
                requestTimeoutMillis = SocketConfiguration.REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = SocketConfiguration.CONNECT_TIMEOUT_MILLIS
                socketTimeoutMillis = SocketConfiguration.SOCKET_TIMEOUT_MILLIS
            }
        }

    // Initialize the repository
    val repository: CryptoRepository = CryptoRepositoryImpl(client, logger)

    // Initialize the EventDispatcherAdapter
    val eventDispatcher = EventDispatcherAdapter()

    // Initialize the service
    val fetchService = FetchCoinMarketDataService(repository, logger, eventDispatcher)

    // Create a SupervisorJob for structured concurrency
    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    var isActive = true

    // Launch the scheduled job
    scope.launch {
        while (isActive) {
            fetchService.fetchAndProcessData()
            delay(FetchCoinMarketDataService.DELAY_MINUTES * FetchCoinMarketDataService.MILLISECONDS_IN_A_MINUTE)
        }
    }

    scope.launch {
        val metricsService = ApiMetricsLoggingService(logger)
        metricsService.startLogging()
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
