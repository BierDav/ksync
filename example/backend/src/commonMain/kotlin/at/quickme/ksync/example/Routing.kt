package at.quickme.ksync.example

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.example.repo.EventLogRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val database by inject<Database>()
    val eventLog by inject<EventLogRepo>()
    routing {
        post("/") {
            val transaction = call.receive<EventTransaction>()
            if (eventLog.countByAuthorSequenceAndAuthor(
                    database.localDb,
                    transaction.authorSequence,
                    transaction.author
                ).getOrElse { 1 } > 0
            )
                return@post call.respond(HttpStatusCode.Conflict)
            database.ksyncServer.incomingTransactions.send(transaction)
            call.respond(HttpStatusCode.OK)
        }
    }
}
