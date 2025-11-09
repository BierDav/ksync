package at.quickme.ksync.example

import KSyncServer
import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.entity.EventLog
import at.quickme.ksync.example.repo.EventLogRepo
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.Koin
import kotlin.reflect.KClass

class Database(
    val eventLogRepo: EventLogRepo,
    val koin: Koin
) {

    val localDb = SQLite("local.db")
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    lateinit var ksyncServer: KSyncServer

    init {
        coroutineScope.launch {
            ksyncServer = KSyncServer(
                localDb,
                eventLogRepo.findOneByLast(localDb).getOrThrow().sequence,
                coroutineScope,
                object : RepositoryProvider {
                    override fun <T : Any> get(clazz: KClass<T>): T = koin.get(clazz)
                },
                {
                    eventLogRepo.insert(this, EventLog(it))
                }
            )
        }
    }
}