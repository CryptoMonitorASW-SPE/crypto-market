package it.unibo

import it.unibo.application.ApiMetricsLoggingService
import it.unibo.application.FetchCoinMarketDataService
import it.unibo.application.FetchProcessManager
import it.unibo.domain.CryptoRepository
import it.unibo.infrastructure.CryptoRepositoryImpl
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.WebServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("CoinGeckoApp")
    // Initialize dependencies
    val repository: CryptoRepository = CryptoRepositoryImpl(logger)
    val eventDispatcher = EventDispatcherAdapter()
    val fetchService = FetchCoinMarketDataService(repository, logger, eventDispatcher)

    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    val fetchProcessManager = FetchProcessManager(fetchService, scope)
    val webServer = WebServer(fetchProcessManager, repository, eventDispatcher).apply { start() }

    // Metrics service
    scope.launch {
        ApiMetricsLoggingService(logger).startLogging()
    }

    // Shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                logger.info("Shutting down...")
                webServer.stop()
                supervisor.cancelAndJoin()
                repository.killClient()
                logger.info("Shutdown complete")
            }
        },
    )

    runBlocking { supervisor.join() }
}
