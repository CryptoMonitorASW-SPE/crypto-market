package it.unibo.domain.ports

import CryptoDetails
import it.unibo.domain.Crypto
import it.unibo.domain.CryptoChartData
import it.unibo.domain.Currency

interface CryptoRepository {
    suspend fun fetchCoinMarkets(currency: Currency): List<Crypto>?

    suspend fun fetchCoinChartData(
        coinId: String,
        currency: Currency,
        days: Int,
    ): CryptoChartData?

    suspend fun fetchCoinDetails(coinId: String): CryptoDetails?

    fun killClient()
}
