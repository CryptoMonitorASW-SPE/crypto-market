package it.unibo.domain

import CryptoDetails

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
