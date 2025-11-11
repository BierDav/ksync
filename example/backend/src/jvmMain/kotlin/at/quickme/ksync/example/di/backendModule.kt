package at.quickme.ksync.example.di

import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.Database
import at.quickme.ksync.example.api.SyncApiImpl
import org.koin.dsl.module
import kotlin.reflect.KClass

val backendModule = module {
    single<RepositoryProvider> {
        object : RepositoryProvider {
            override fun <T : Any> resolve(clazz: KClass<T>): T = get(clazz)
        }
    }
    single { Database(get(), get()) }
    single { SyncApiImpl(get(), get()) }
}