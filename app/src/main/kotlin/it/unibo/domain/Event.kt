package it.unibo.domain

import kotlinx.serialization.Serializable

@Serializable(with = EventTypeSerializer::class)
data class EventType(
    val type: String,
) {
    companion object {
        val CRYPTO_UPDATE_USD = EventType("CRYPTO_UPDATE_USD")
        val CRYPTO_UPDATE_EUR = EventType("CRYPTO_UPDATE_EUR")
    }
}

@Serializable
data class EventPayload(
    val eventType: EventType,
    val payload: List<Crypto>,
)
