package at.quickme.ksync.example

import at.quickme.ksync.example.api.SyncApi
import at.quickme.ksync.example.api.SyncApiImpl
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.ext.inject

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSockets() {
    val syncApi by inject<SyncApiImpl>()

    install(Krpc) {
        serialization {
            cbor()
        }
    }
    routing {
        rpc {
            registerService<SyncApi> { syncApi }
        }
    }
}
