package it.unibo

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import it.unibo.application.ApiMetricsLoggingService
import it.unibo.application.FetchCoinMarketDataService
import it.unibo.application.FetchProcessManager
import it.unibo.domain.CryptoRepository
import it.unibo.infrastructure.CryptoRepositoryImpl
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.WebServer
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("CoinGeckoApp")
    // Configure Ktor client
    val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000L
            connectTimeoutMillis = 5_000L
            socketTimeoutMillis = 5_000L
        }
    }

    // Initialize dependencies
    val repository: CryptoRepository = CryptoRepositoryImpl(client, logger)
    val eventDispatcher = EventDispatcherAdapter()
    val fetchService = FetchCoinMarketDataService(repository, logger, eventDispatcher)

    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    val fetchProcessManager = FetchProcessManager(fetchService, scope)
    val webServer = WebServer(fetchProcessManager).apply { start() }

    // Metrics service
    scope.launch {
        ApiMetricsLoggingService(logger).startLogging()
    }

    // Shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            logger.info("Shutting down...")
            webServer.stop()
            supervisor.cancelAndJoin()
            client.close()
            logger.info("Shutdown complete")
        }
    })

    runBlocking { supervisor.join() }

}