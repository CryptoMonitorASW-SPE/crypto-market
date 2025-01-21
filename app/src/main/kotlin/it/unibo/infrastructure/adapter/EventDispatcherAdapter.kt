package it.unibo.infrastructure.adapter

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.unibo.domain.Crypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

class EventDispatcherAdapter(
    private val httpServerHost: String = "event-dispatcher",
    private val httpServerPort: Int = 3000,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val logger = LoggerFactory.getLogger(EventDispatcherAdapter::class.java)
    private val client = HttpClient(CIO)
    private val mutex = Mutex()

    fun publish(data: Crypto) {
        scope.launch {
            mutex.withLock {
                try {
                    val eventType = "PRICE_UPDATED"
                    val cryptoId = "bitcoin"
                    val newPrice = 45
                    val timestamp = 1633024800

                    val event =
                        """
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
                    // val jsonData = Json.encodeToString(data)
                    val response: HttpResponse =
                        client.post {
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
                } catch (e: IOException) {
                    logger.error("Failed to publish data due to network error", e)
                } catch (e: SerializationException) {
                    logger.error("Failed to publish data due to serialization error", e)
                }
            }
        }
    }

    fun close() {
        client.close()
        logger.info("HTTP client closed")
    }
}
