package at.quickme.ksync.example.di

import at.quickme.ksync.example.codegen2.BaseEventLogRepoImpl
import at.quickme.ksync.example.codegen2.OutgoingEventLogRepoImpl
import org.koin.dsl.module
import at.quickme.ksync.example.repo.BaseEventLogRepo
import at.quickme.ksync.example.repo.OutgoingEventLogRepo

val appRepoModule = module {
    single<BaseEventLogRepo> { BaseEventLogRepoImpl }
    single<OutgoingEventLogRepo> { OutgoingEventLogRepoImpl }
}