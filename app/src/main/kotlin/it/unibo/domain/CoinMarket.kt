package it.unibo.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinMarket(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("market_cap") val marketCap: Long,
    @SerialName("market_cap_rank") val marketCapRank: Int,
    @SerialName("total_volume") val totalVolume: Long,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerialName("last_updated") val lastUpdated: String,
)
