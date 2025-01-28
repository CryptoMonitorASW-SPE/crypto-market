package it.unibo.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class CurrencyValue(
    @Serializable(with = CurrencyMapSerializer::class)
    val values: Map<Currency, Double?>,
)

@Serializable(with = CurrencySerializer::class)
sealed class Currency(
    val code: String,
) {
    @SerialName("usd")
    data object USD : Currency("usd")

    @SerialName("eur")
    data object EUR : Currency("eur")

    companion object {
        fun getAllCurrencies(): List<Currency> = listOf(USD, EUR)

        fun fromCode(code: String): Currency {
            val upperCaseCode = code.uppercase()
            return getAllCurrencies().first { it.code.uppercase() == upperCaseCode }
        }
    }
}

object CurrencySerializer : KSerializer<Currency> {
    override val descriptor = PrimitiveSerialDescriptor("Currency", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Currency,
    ) {
        encoder.encodeString(value.code)
    }

    override fun deserialize(decoder: Decoder): Currency {
        val code = decoder.decodeString()
        return Currency.fromCode(code)
    }
}

object CurrencyMapSerializer : KSerializer<Map<Currency, Double?>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("CurrencyMap") {
            element<Map<Currency, Double?>>("values")
        }

    override fun serialize(
        encoder: Encoder,
        value: Map<Currency, Double?>,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("This class can be serialized only by JSON")
        val jsonObject =
            buildJsonObject {
                value.forEach { (currency, amount) ->
                    put(currency.code, amount ?: 0.0) // Provide a default value of 0.0 for null amounts
                }
            }
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): Map<Currency, Double?> {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("This class can be deserialized only by JSON")
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        return jsonObject
            .map { (key, value) ->
                Currency.fromCode(key) to value.jsonPrimitive.doubleOrNull
            }.toMap()
    }
}
