package it.unibo.domain.ports

import CryptoDetails
import it.unibo.domain.Crypto
import it.unibo.domain.CryptoChartData
import it.unibo.domain.Currency

/**
 * Repository interface for fetching cryptocurrency data.
 *
 * Provides methods to retrieve market data, chart data, detailed information,
 * and to terminate any underlying API client.
 */
interface CryptoRepository {
    /**
     * Fetches coin market data based on the specified currency.
     *
     * @param currency the currency for which the market data is requested.
     * @return a list of [Crypto] objects representing the market data, or null in case of failure.
     */
    suspend fun fetchCoinMarkets(currency: Currency): List<Crypto>?

    /**
     * Fetches chart data for the specified cryptocurrency.
     *
     * @param coinId the identifier of the cryptocurrency.
     * @param currency the currency in which the chart data should be represented.
     * @param days the number of days for which the chart data is requested.
     * @return a [CryptoChartData] object containing the chart data, or null in case of failure.
     */
    suspend fun fetchCoinChartData(
        coinId: String,
        currency: Currency,
        days: Int,
    ): CryptoChartData?

    /**
     * Fetches detailed information for the specified cryptocurrency.
     *
     * @param coinId the identifier of the cryptocurrency.
     * @return a [CryptoDetails] object containing detailed information, or null in case of failure.
     */
    suspend fun fetchCoinDetails(coinId: String): CryptoDetails?

    /**
     * Terminates the underlying client or connection used for fetching data.
     */
    fun killClient()
}
