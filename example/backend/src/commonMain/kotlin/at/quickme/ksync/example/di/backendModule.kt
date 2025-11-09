package at.quickme.ksync.example.di

import at.quickme.ksync.example.Database
import org.koin.dsl.module

val backendModule = module {
    single { Database(get(),get()) }
}