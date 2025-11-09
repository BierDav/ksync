package at.quickme.ksync.example.db

import KSyncClient
import at.quickme.ksync.example.entity.OutgoingEventLog
import at.quickme.ksync.RepositoryProvider
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.Koin
import at.quickme.ksync.example.repo.BaseEventLogRepo
import at.quickme.ksync.example.repo.OutgoingEventLogRepo
import kotlin.reflect.KClass
import kotlin.uuid.Uuid
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Database(
    val eventLogRepo: BaseEventLogRepo,
    val outgoingEventLogRepo: OutgoingEventLogRepo,
    val koin: Koin
) {
    val baseDb: Driver
    val localDb: Driver
    val authorId = Uuid.random()
    var localDbCounter = 0

    lateinit var syncClient: KSyncClient
    val ktorClient = HttpClient(CIO)

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        baseDb = SQLite("base.db")
        localDb = SQLite("local-$authorId-$localDbCounter.db")

        coroutineScope.launch {
            syncClient = KSyncClient(
                authorId,
                eventLogRepo.findOneByLast(baseDb).getOrThrow().sequence,
                outgoingEventLogRepo.findAllNotConfirmed(localDb).getOrThrow().map { it.event },
                localDb,
                coroutineScope,
                object : RepositoryProvider {
                    override fun <T : Any> get(clazz: KClass<T>): T = koin.get(clazz)
                },
                {
                    error("Rebase not implemented")
                    val newFileName = "local-$authorId-${++localDbCounter}.db"
                    eventLogRepo.vacuum(baseDb, newFileName)
                    val newLocalDb = SQLite(newFileName)
                },
                { transaction ->
                    outgoingEventLogRepo.insert(this, OutgoingEventLog(event = transaction))
                }
            )
        }

    }
}