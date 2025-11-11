package at.quickme.ksync.example.di

import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.db.Database
import org.koin.dsl.module
import kotlin.reflect.KClass

val appModule = module {
    single<RepositoryProvider> {
        object : RepositoryProvider {
            override fun <T : Any> resolve(clazz: KClass<T>): T = get(clazz)
        }
    }
    single { Database(get(), get(), get()) }
}