package at.quickme.ksync.example.di

import at.quickme.ksync.example.codegen2.EventLogRepoImpl
import at.quickme.ksync.example.repo.EventLogRepo
import org.koin.dsl.module

val backendRepoModule = module {
    single<EventLogRepo> { EventLogRepoImpl }
}