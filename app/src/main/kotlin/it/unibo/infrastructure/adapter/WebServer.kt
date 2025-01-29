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

    private suspend fun handleStart(
        manager: FetchProcessManager,
        eventDispatcher: EventDispatcherAdapter,
        call: ApplicationCall,
        currency: Currency,
    ) {
        if (manager.isRunning(currency)) {
            val latestData = manager.getLatestData(currency)
            if (latestData != null) {
                eventDispatcher.publish(latestData)
                call.respond(
                    mapOf(
                        "status" to "already running",
                        "currency" to currency.code,
                        "data" to "Data sent to event dispatcher",
                    ),
                )
            } else {
                call.respond(
                    mapOf(
                        "status" to "already running",
                        "currency" to currency.code,
                        "data" to "No data available",
                    ),
                )
            }
        } else {
            manager.start(currency)
            call.respond(mapOf("status" to "started", "currency" to currency.code))
        }
    }

    private val server =
        embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) { json() }
            routing {
                post("/start") {
                    val currencyParam = call.parameters["currency"] ?: "USD"
                    val currency = Currency.fromCode(currencyParam)
                    handleStart(manager, eventDispatcher, call, currency)
                }

                post("/stop") {
                    val currencyParam = call.parameters["currency"] ?: "USD"
                    val currency = Currency.fromCode(currencyParam)
                    manager.stop(currency)
                    call.respond(mapOf("status" to "stopped", "currency" to currency.code))
                }

                get("/status") {
                    val statuses = Currency.getAllCurrencies().associateWith { manager.isRunning(it) }
                    call.respond(statuses)
                }

                get("/data") {
                    val currencyParam = call.parameters["currency"] ?: "USD"
                    val currency = Currency.fromCode(currencyParam)
                    val data = manager.getLatestData(currency)
                    if (data != null) {
                        call.respond(data)
                    } else {
                        call.respond(HttpStatusCode.NoContent)
                    }
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

                get("/details/{coinId}") {
                    val coinId =
                        call.parameters["coinId"]
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or malformed coinId")
                    val details =
                        runBlocking {
                            repository.fetchCoinDetails(coinId)
                        }
                    if (details != null) {
                        call.respond(details)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Details not found")
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
