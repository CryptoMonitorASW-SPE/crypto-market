package it.unibo.domain

interface CryptoRepository {
    suspend fun fetchCoinMarkets(currency: Currency): List<CryptoSerializable>?
    fun killClient()
}
