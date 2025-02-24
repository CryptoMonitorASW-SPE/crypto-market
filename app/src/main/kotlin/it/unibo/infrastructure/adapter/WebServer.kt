@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import it.unibo.domain.Currency
import it.unibo.domain.ports.CryptoRepository
import it.unibo.domain.ports.EventDispatcher
import it.unibo.domain.ports.FetchProcess
import kotlinx.coroutines.runBlocking

/**
 * WebServer provides HTTP endpoints to control and monitor the data fetching process.
 */
class WebServer(
    private val manager: FetchProcess,
    private val repository: CryptoRepository,
    private val eventDispatcher: EventDispatcher,
) {
    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

    /**
     * Handles the "POST /start" endpoint.
     *
     * <p>If the fetch process is already running for the specified currency:</p>
     * <ul>
     *  <li>If latest data is available, it publishes the data via the event dispatcher and responds
     *      with a message indicating the data was sent.</li>
     *  <li>If no data is available, it responds with a "no data available" message.</li>
     * </ul>
     * <p>If the process is not running, it starts the process and responds with the "started" status.</p>
     *
     * @param call the application call.
     */
    private suspend fun postStart(call: ApplicationCall) {
        val currencyParam = call.parameters["currency"] ?: "USD"
        val currency = Currency.fromCode(currencyParam)
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

    /**
     * Handles the "POST /stop" endpoint.
     *
     * Stops the fetch process for the specified currency.
     *
     * @param call the application call.
     */
    private suspend fun postStop(call: ApplicationCall) {
        val currencyParam = call.parameters["currency"] ?: "USD"
        val currency = Currency.fromCode(currencyParam)
        manager.stop(currency)
        call.respond(mapOf("status" to "stopped", "currency" to currency.code))
    }

    /**
     * Handles the "GET /status" endpoint.
     *
     * Returns the running status for all supported currencies.
     *
     * @param call the application call.
     */
    private suspend fun getStatus(call: ApplicationCall) {
        val statuses = Currency.getAllCurrencies().associateWith { manager.isRunning(it) }
        call.respond(statuses)
    }

    /**
     * Handles the "GET /data" endpoint.
     *
     * Returns the latest data available for the specified currency.
     * Responds with HTTP 204 if no data is available.
     *
     * @param call the application call.
     */
    private suspend fun getData(call: ApplicationCall) {
        val currencyParam = call.parameters["currency"] ?: "USD"
        val currency = Currency.fromCode(currencyParam)
        val data = manager.getLatestData(currency)
        if (data != null) {
            call.respond(data)
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    /**
     * Handles the "GET /health" endpoint.
     *
     * Provides a simple health-check response.
     *
     * @param call the application call.
     */
    private suspend fun getHealth(call: ApplicationCall) {
        call.respond(mapOf("status" to "healthy"))
    }

    /**
     * Handles the "GET /chart/{coinId}/{currency}/{days}" endpoint.
     *
     * Retrieves chart data for the specified cryptocurrency.
     *
     * @param call the application call.
     */
    private suspend fun getChart(call: ApplicationCall) {
        val coinId = call.parameters["coinId"]
        val currencyParam = call.parameters["currency"]
        val days = call.parameters["days"]?.toIntOrNull()

        if (coinId == null || currencyParam == null || days == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing or malformed parameters")
            return
        }

        val chartData =
            runBlocking {
                repository.fetchCoinChartData(coinId, Currency.fromCode(currencyParam), days)
            }
        if (chartData != null) {
            call.respond(chartData)
        } else {
            call.respond(HttpStatusCode.NotFound, "Chart data not found")
        }
    }

    /**
     * Handles the "GET /details/{coinId}" endpoint.
     *
     * Retrieves detailed information for the specified cryptocurrency.
     *
     * @param call the application call.
     */
    private suspend fun getDetails(call: ApplicationCall) {
        val coinId =
            call.parameters["coinId"]
                ?: return call.respond(HttpStatusCode.BadRequest, "Missing or malformed coinId")
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

    private val server =
        embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) { json() }
            routing {
                post("/start") {
                    postStart(call)
                }
                post("/stop") {
                    postStop(call)
                }
                get("/status") {
                    getStatus(call)
                }
                get("/data") {
                    getData(call)
                }
                get("/health") {
                    getHealth(call)
                }
                get("/chart/{coinId}/{currency}/{days}") {
                    getChart(call)
                }
                get("/details/{coinId}") {
                    getDetails(call)
                }
            }
        }

    /**
     * Starts the web server.
     */
    fun start() {
        server.start(wait = true)
    }

    /**
     * Stops the web server.
     */
    fun stop() {
        server.stop(GRACE_PERIOD, TIMEOUT)
    }
}
