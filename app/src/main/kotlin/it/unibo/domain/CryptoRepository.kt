package it.unibo.domain

interface CryptoRepository {
    suspend fun fetchCoinMarkets(currency: Currency): List<CryptoSerializable>?

    suspend fun fetchCoinChartData(
        coinId: String,
        currency: Currency,
        days: Int,
    ): CryptoChartData?

    fun killClient()
}
