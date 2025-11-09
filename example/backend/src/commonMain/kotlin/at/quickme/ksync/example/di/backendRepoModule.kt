package at.quickme.ksync.example.di

import at.quickme.ksync.example.repo.EventLogRepo
import di.repoModule
import org.koin.dsl.module

val backendRepoModule = module {
    single<EventLogRepo> { EventLogRepoImpl }
}