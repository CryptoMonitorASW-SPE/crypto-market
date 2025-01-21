package it.unibo.infrastructure.adapter

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.unibo.domain.CoinMarket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class EventDispatcherAdapter(
    private val httpServerHost: String = "event-dispatcher",
    private val httpServerPort: Int = 3000,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val logger = LoggerFactory.getLogger(EventDispatcherAdapter::class.java)
    private val client = HttpClient(CIO)
    private val mutex = Mutex()

    fun publish(data: CoinMarket) {
        scope.launch {
            mutex.withLock {
                try {
                    val eventType = "PRICE_UPDATED"
                    val cryptoId = "bitcoin"
                    val newPrice = 45
                    val timestamp = 1633024800

                    val event = """
                        {
                            "eventType": "$eventType",
                            "payload": {
                                "cryptoId": "$cryptoId",
                                "newPrice": $newPrice
                            },
                            "timestamp": $timestamp
                        }
                    """.trimIndent()
                    val jsonData = event
                    //val jsonData = Json.encodeToString(data)
                    val response: HttpResponse = client.post {
                        url {
                            protocol = URLProtocol.HTTP
                            host = httpServerHost
                            port = httpServerPort
                            encodedPath = "/realtime/events"
                        }
                        contentType(ContentType.Application.Json)
                        setBody(jsonData)
                    }
                    logger.info("Published data: $jsonData, Response: ${response.status}")
                } catch (e: Exception) {
                    logger.error("Failed to publish data", e)
                }
            }
        }
    }

    fun close() {
        client.close()
        logger.info("HTTP client closed")
    }
}