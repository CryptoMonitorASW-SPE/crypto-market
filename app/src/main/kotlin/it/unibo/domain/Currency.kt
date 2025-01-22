@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.domain

import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

        fun fromCode(code: String): Currency = getAllCurrencies().first { it.code == code }
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

object CurrencyMapSerializer : KSerializer<Map<Currency, Double>> {
    private val delegateSerializer = MapSerializer(CurrencySerializer, Double.serializer())

    override val descriptor = delegateSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Map<Currency, Double>,
    ) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<Currency, Double> = delegateSerializer.deserialize(decoder)
}
