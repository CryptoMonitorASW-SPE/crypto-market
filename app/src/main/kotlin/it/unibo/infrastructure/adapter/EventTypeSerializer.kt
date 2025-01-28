package it.unibo.infrastructure.adapter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EventTypeSerializer : KSerializer<EventType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EventType", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: EventType,
    ) {
        encoder.encodeString(value.type)
    }

    override fun deserialize(decoder: Decoder): EventType = EventType(decoder.decodeString())
}
