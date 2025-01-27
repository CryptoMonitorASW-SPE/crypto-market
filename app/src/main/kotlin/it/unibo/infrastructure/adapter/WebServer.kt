package it.unibo.infrastructure.adapter

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import it.unibo.application.FetchProcessManager
import it.unibo.domain.CryptoRepository
import it.unibo.domain.Currency
import kotlinx.coroutines.runBlocking

class WebServer(
    private val manager: FetchProcessManager,
    private val repository: CryptoRepository,
    private val eventDispatcher: EventDispatcherAdapter,
) {
    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

    private val server =
        embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) { json() }
            routing {
                post("/start") {
                    if (manager.isRunning) {
                        val latestData = manager.getLatestData()
                        if (latestData != null) {
                            eventDispatcher.publish(latestData)
                            call.respond(
                                mapOf(
                                    "status" to "already running",
                                    "data" to "Data sent to event dispatcher",
                                ),
                            )
                        } else {
                            call.respond(
                                mapOf(
                                    "status" to "already running",
                                    "data" to "No data available",
                                ),
                            )
                        }
                    } else {
                        manager.start()
                        call.respond(mapOf("status" to "started"))
                    }
                }
                post("/stop") {
                    manager.stop()
                    call.respond(mapOf("status" to "stopped"))
                }
                get("/status") {
                    call.respond(mapOf("status" to if (manager.isRunning) "running" else "stopped"))
                }
                get("/health") {
                    call.respond(mapOf("status" to "healthy"))
                }
                get("/chart/{coinId}/{currency}/{days}") {
                    val coinId =
                        call.parameters["coinId"]
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or malformed coinId")
                    val currency =
                        call.parameters["currency"]
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or malformed currency")
                    val days =
                        call.parameters["days"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or malformed days")

                    val chartData =
                        runBlocking {
                            repository.fetchCoinChartData(
                                coinId,
                                Currency.fromCode(currency),
                                days,
                            )
                        }
                    if (chartData != null) {
                        call.respond(chartData)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Chart data not found")
                    }
                }
            }
        }

    fun start() {
        server.start(wait = true)
    }

    fun stop() {
        server.stop(GRACE_PERIOD, TIMEOUT)
    }
}
