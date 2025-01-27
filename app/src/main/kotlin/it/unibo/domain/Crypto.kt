package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable
data class Crypto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val prices: CurrencyValue?,
    val marketCap: CurrencyValue?,
    val marketCapRank: Int?,
    val fullyDilutedValuation: CurrencyValue?,
    val totalVolume: CurrencyValue?,
    val high24h: CurrencyValue?,
    val low24h: CurrencyValue?,
    val priceChange24h: CurrencyValue?,
    val priceChangePercentage24h: Double?,
    val marketCapChange24h: CurrencyValue?,
    val marketCapChangePercentage24h: Double?,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val ath: CurrencyValue?,
    val athChangePercentage: Double?,
    val athDate: String?,
    val atl: CurrencyValue?,
    val atlChangePercentage: Double?,
    val atlDate: String?,
    val lastUpdated: String,
)
