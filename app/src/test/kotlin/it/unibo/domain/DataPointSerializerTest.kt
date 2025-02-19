package it.unibo.domain

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DataPointSerializerTest {
    @Test
    fun testDeserializationOfDataPoints() {
        // JSON returned by CoinGecko API
        val jsonData =
            """
            [
                [1739890800000,92378.78,92378.78,91333.99,91534.76],
                [1739892600000,91369.62,91475.85,91207.34,91422.63],
                [1739894400000,91226.34,91505.72,91120.86,91235.14]
            ]
            """.trimIndent()

        val expectedDataPoints =
            listOf(
                DataPoint(1739890800000, 92378.78, 92378.78, 91333.99, 91534.76),
                DataPoint(1739892600000, 91369.62, 91475.85, 91207.34, 91422.63),
                DataPoint(1739894400000, 91226.34, 91505.72, 91120.86, 91235.14),
            )

        val actualDataPoints: List<DataPoint> = Json.decodeFromString(ListSerializer(DataPointSerializer), jsonData)

        assertEquals(expectedDataPoints, actualDataPoints)
    }
}
