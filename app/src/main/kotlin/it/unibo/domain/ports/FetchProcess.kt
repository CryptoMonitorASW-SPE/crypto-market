package it.unibo.domain.ports

import it.unibo.domain.Currency
import it.unibo.domain.EventPayload

/**
 * Interface for managing the fetch process of coin market data.
 */
interface FetchProcess {
    /**
     * Checks if the fetch process is running for the given currency.
     *
     * @param currency The currency to check.
     * @return `true` if the fetch process is running, `false` otherwise.
     */
    fun isRunning(currency: Currency): Boolean

    /**
     * Starts the fetch process for the given currency.
     *
     * @param currency The currency for which to start the fetch process.
     */
    fun start(currency: Currency)

    /**
     * Stops the fetch process for the given currency.
     *
     * @param currency The currency for which to stop the fetch process.
     */
    fun stop(currency: Currency)

    /**
     * Stops all fetch processes.
     */
    fun stopAll()

    /**
     * Gets the latest data for the given currency.
     *
     * @param currency The currency for which to get the latest data.
     * @return The latest [EventPayload] for the given currency, or `null` if no data is available.
     */
    fun getLatestData(currency: Currency): EventPayload?
}
