package it.unibo.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FetchProcessManager(
    private val fetchService: FetchCoinMarketDataService,
    private val scope: CoroutineScope,
) {
    private var fetchJob: Job? = null
    val isRunning: Boolean get() = fetchJob?.isActive ?: false

    fun start() {
        if (isRunning) return
        fetchJob = scope.launch {
            while (true) {
                fetchService.fetchAndProcessData()
                delay(FetchCoinMarketDataService.DELAY_MINUTES * 60_000L)
            }
        }
    }

    fun stop() {
        fetchJob?.cancel()
        fetchJob = null
    }
}