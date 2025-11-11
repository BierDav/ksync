package at.quickme.ksync.example

import at.quickme.ksync.KSyncServer
import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.codegen.RepositoryEventSource
import at.quickme.ksync.example.entity.EventLog
import at.quickme.ksync.example.repo.EventLogRepo
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.Statement
import io.github.smyrgeorge.sqlx4k.impl.hook.HookEventBus
import io.github.smyrgeorge.sqlx4k.impl.metadata.MetadataStorage
import io.github.smyrgeorge.sqlx4k.invalidation.applyInvalidationHandler
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import kotlin.reflect.KClass

class Database(
    val eventLogRepo: EventLogRepo,
    val repositoryProvider: RepositoryProvider
) : QueryExecutor{

    val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val localDb: Deferred<Driver> = coroutineScope.async {
        val db = SQLite("db/base.db")
        db.migrate(
            "../shared/migrations",
            "_sqlx4k_migrations",
        ).getOrThrow()
        db
    }

    var ksyncServer: Deferred<KSyncServer<RepositoryEventSource>> = coroutineScope.async {
        val local = localDb.await()
        KSyncServer(
            local,
            eventLogRepo.findOneByLast(local).getOrThrow()?.sequence ?: -1,
            coroutineScope,
            repositoryProvider,
            {
                eventLogRepo.insert(this, EventLog(it)).getOrThrow().event()
            }
        )
    }

    override val encoders: Statement.ValueEncoderRegistry
        get() = runBlocking { localDb.await().encoders }
    override val metadata: MetadataStorage
        get() = runBlocking { localDb.await().metadata }

    override suspend fun execute(sql: String): Result<Long> {
        return localDb.await().execute(sql)
    }

    override suspend fun fetchAll(sql: String): Result<ResultSet> {
        return localDb.await().fetchAll(sql)
    }
}