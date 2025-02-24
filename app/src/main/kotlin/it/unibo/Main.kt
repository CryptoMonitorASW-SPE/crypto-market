package it.unibo

import it.unibo.application.FetchCoinMarketDataService
import it.unibo.application.FetchProcessManager
import it.unibo.domain.ports.CryptoRepository
import it.unibo.domain.ports.EventDispatcher
import it.unibo.domain.ports.FetchCoinMarketData
import it.unibo.domain.ports.FetchProcess
import it.unibo.infrastructure.adapter.CryptoRepositoryImpl
import it.unibo.infrastructure.adapter.EventDispatcherAdapter
import it.unibo.infrastructure.adapter.WebServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("CoinGeckoApp")
    // Initialize dependencies
    val repository: CryptoRepository = CryptoRepositoryImpl(logger)
    val eventDispatcher: EventDispatcher = EventDispatcherAdapter()
    val fetchService: FetchCoinMarketData = FetchCoinMarketDataService(repository, logger, eventDispatcher)

    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    val fetchProcessManager: FetchProcess = FetchProcessManager(fetchService, scope)
    val webServer = WebServer(fetchProcessManager, repository, eventDispatcher).apply { start() }

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
