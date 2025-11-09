package at.quickme.ksync.example

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    val database by inject<Database>()

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/receive") { // websocketSession
            database.ksyncServer.receiveOutgoingTransactionsAsFlow().collect {
                sendSerialized(it)
            }
        }
    }
}
