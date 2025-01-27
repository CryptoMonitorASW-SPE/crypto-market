package it.unibo.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

object DataPointSerializer : KSerializer<DataPoint> {
    private const val TIMESTAMP_INDEX = 0
    private const val OPEN_INDEX = 1
    private const val HIGH_INDEX = 2
    private const val LOW_INDEX = 3
    private const val CLOSE_INDEX = 4

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("DataPoint") {
            element<Long>("timestamp")
            element<Double>("open")
            element<Double>("high")
            element<Double>("low")
            element<Double>("close")
        }

    override fun deserialize(decoder: Decoder): DataPoint {
        val input =
            decoder as? JsonDecoder
                ?: throw SerializationException("This class can be loaded only by JSON")
        val jsonArray = input.decodeJsonElement().jsonArray

        return DataPoint(
            timestamp = jsonArray[0].jsonPrimitive.long,
            open = jsonArray[1].jsonPrimitive.double,
            high = jsonArray[2].jsonPrimitive.double,
            low = jsonArray[3].jsonPrimitive.double,
            close = jsonArray[4].jsonPrimitive.double,
        )
    }

    override fun serialize(
        encoder: Encoder,
        value: DataPoint,
    ) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeLongElement(descriptor, TIMESTAMP_INDEX, value.timestamp)
        composite.encodeDoubleElement(descriptor, OPEN_INDEX, value.open)
        composite.encodeDoubleElement(descriptor, HIGH_INDEX, value.high)
        composite.encodeDoubleElement(descriptor, LOW_INDEX, value.low)
        composite.encodeDoubleElement(descriptor, CLOSE_INDEX, value.close)
        composite.endStructure(descriptor)
    }
}
