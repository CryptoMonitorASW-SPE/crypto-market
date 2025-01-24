package it.unibo.infrastructure.adapter

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import it.unibo.application.FetchProcessManager

class WebServer(private val manager: FetchProcessManager) {
    private val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        routing {
            post("/start") {
                    manager.start()
                    call.respond(mapOf("status" to "started"))
                }
                post("/stop") {
                    manager.stop()
                    call.respond(mapOf("status" to "stopped"))
                }
                get("/status") {
                    call.respond(mapOf("status" to if (manager.isRunning) "running" else "stopped"))
                }
        }
}

    fun start() {
        server.start(wait = true)
    }

    fun stop() {
        server.stop(1000, 5000)
    }
}