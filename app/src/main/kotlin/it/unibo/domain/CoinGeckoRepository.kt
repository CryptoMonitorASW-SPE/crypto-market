package it.unibo.domain

interface CoinGeckoRepository {
    suspend fun fetchCoinMarkets(): List<CoinMarket>?
}
