package it.unibo.domain

import kotlinx.serialization.Serializable

// @Serializable
// data class CurrencyValue(
//    @SerialName("usd")
//    val usd: Double? = null,
//    @SerialName("eur")
//    val eur: Double? = null,
// )
//
// @Serializable
// sealed class Currency(
//    val code: String,
// ) {
//    @Serializable
//    @SerialName("usd")
//    data object USD : Currency("usd")
//
//    @Serializable
//    @SerialName("eur")
//    data object EUR : Currency("eur")
//
//    companion object {
//        fun getAllCurrencies(): List<Currency> = listOf(USD, EUR)
//    }
// }

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
