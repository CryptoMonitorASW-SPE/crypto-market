package it.unibo.domain

import CryptoDetails

interface CryptoRepository {
    // TODO: Return already with Crypto Type
    suspend fun fetchCoinMarkets(currency: Currency): List<CryptoSerializable>?

    suspend fun fetchCoinChartData(
        coinId: String,
        currency: Currency,
        days: Int,
    ): CryptoChartData?

    suspend fun fetchCoinDetails(coinId: String): CryptoDetails?

    fun killClient()
}
