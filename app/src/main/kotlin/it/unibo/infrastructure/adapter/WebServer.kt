package it.unibo.infrastructure.adapter

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import it.unibo.application.FetchProcessManager

class WebServer(
    private val manager: FetchProcessManager,
    private val eventDispatcher: EventDispatcherAdapter
) {

    companion object {
        const val PORT = 8080
        const val GRACE_PERIOD = 1000L
        const val TIMEOUT = 5000L
    }

    private val server = embeddedServer(Netty, port = PORT) {
        install(ContentNegotiation) { json() }
        routing {
            post("/start") {
                if (manager.isRunning) {
                    val latestData = manager.getLatestData()
                    if (latestData != null) {
                        eventDispatcher.publish(latestData)
                        call.respond(mapOf("status" to "already running", "data" to "Data sent to event dispatcher"))
                    } else {
                        call.respond(mapOf("status" to "already running", "data" to "No data available"))
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
        }
    }

    fun start() {
        server.start(wait = true)
    }

    fun stop() {
        server.stop(GRACE_PERIOD, TIMEOUT)
    }
}

