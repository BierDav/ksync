package at.quickme.ksync.example

import at.quickme.ksync.example.di.backendModule
import at.quickme.ksync.example.di.backendRepoModule
import at.quickme.ksync.example.di.repoModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation){
        json()
    }
    install(Koin) {
        modules(repoModule, backendRepoModule,backendModule)
    }

    configureSockets()
}
