package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable(with = DataPointSerializer::class)
data class DataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)

@Serializable
data class CryptoChartData(
    val coinId: String,
    val currency: Currency,
    val timespan: Int,
    val dataPoints: List<DataPoint>,
    val timestamp: Long,
)
