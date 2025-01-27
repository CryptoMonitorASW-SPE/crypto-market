package it.unibo.application

import it.unibo.domain.Crypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FetchProcessManager(
    private val fetchService: FetchCoinMarketDataService,
    private val scope: CoroutineScope,
) {
    private var fetchJob: Job? = null
    private var latestData: List<Crypto>? = null
    val isRunning: Boolean get() = fetchJob?.isActive ?: false

    fun start() {
        if (isRunning) return
        fetchJob =
            scope.launch {
                while (true) {
                    latestData = fetchService.fetchAndProcessData()
                    delay(FetchCoinMarketDataService.DELAY_MINUTES * MINUTES_TO_MS)
                }
            }
    }

    fun stop() {
        fetchJob?.cancel()
        fetchJob = null
    }

    fun getLatestData(): List<Crypto>? = latestData

    companion object {
        const val MINUTES_TO_MS = 60_000L
    }
}
