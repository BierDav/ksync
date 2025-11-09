package at.quickme.ksync.example

import KSyncServer
import at.quickme.ksync.example.di.backendModule
import at.quickme.ksync.example.di.backendRepoModule
import di.repoModule
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        modules(repoModule, backendRepoModule,backendModule)
    }

    configureSockets()
    configureRouting()
}
