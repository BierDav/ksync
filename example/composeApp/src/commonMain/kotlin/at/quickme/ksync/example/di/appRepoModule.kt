package at.quickme.ksync.example.di

import org.koin.dsl.module
import at.quickme.ksync.example.repo.BaseEventLogRepo
import at.quickme.ksync.example.repo.OutgoingEventLogRepo
import at.ksync.example.codegen.*

val appRepoModule = module {
    single<BaseEventLogRepo> { BaseEventLogRepoImpl }
    single<OutgoingEventLogRepo> { OutgoingEventLogRepoImpl }
}