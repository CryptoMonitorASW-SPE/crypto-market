package it.unibo.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CryptoSerializable(
    val id: String,
    @SerialName("symbol")
    val symbol: String,
    val name: String,
    val image: String? = null,
    @SerialName("current_price")
    val currentPrice: Double,
    @SerialName("market_cap")
    val marketCap: Long? = null,
    @SerialName("market_cap_rank")
    val marketCapRank: Int? = null,
    @SerialName("fully_diluted_valuation")
    val fullyDilutedValuation: Long? = null,
    @SerialName("total_volume")
    val totalVolume: Long? = null,
    @SerialName("high_24h")
    val high24h: Double? = null,
    @SerialName("low_24h")
    val low24h: Double? = null,
    @SerialName("price_change_24h")
    val priceChange24h: Double? = null,
    @SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
    @SerialName("market_cap_change_24h")
    val marketCapChange24h: Double? = null,
    @SerialName("market_cap_change_percentage_24h")
    val marketCapChangePercentage24h: Double? = null,
    @SerialName("circulating_supply")
    val circulatingSupply: Double? = null,
    @SerialName("total_supply")
    val totalSupply: Double? = null,
    @SerialName("max_supply")
    val maxSupply: Double? = null,
    val ath: Double? = null,
    @SerialName("ath_change_percentage")
    val athChangePercentage: Double? = null,
    @SerialName("ath_date")
    val athDate: String? = null,
    val atl: Double? = null,
    @SerialName("atl_change_percentage")
    val atlChangePercentage: Double? = null,
    @SerialName("atl_date")
    val atlDate: String? = null,
    @SerialName("last_updated")
    val lastUpdated: String,
)
