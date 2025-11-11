package at.quickme.ksync.example.db

import at.quickme.ksync.KSyncClient
import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.api.SyncApi
import at.quickme.ksync.example.codegen.RepositoryEventSource
import at.quickme.ksync.example.entity.OutgoingEventLog
import at.quickme.ksync.example.repo.BaseEventLogRepo
import at.quickme.ksync.example.repo.OutgoingEventLogRepo
import io.github.smyrgeorge.sqlx4k.*
import io.github.smyrgeorge.sqlx4k.impl.hook.HookApi
import io.github.smyrgeorge.sqlx4k.impl.hook.HookEventBus
import io.github.smyrgeorge.sqlx4k.impl.metadata.MetadataStorage
import io.github.smyrgeorge.sqlx4k.invalidation.applyInvalidationHandler
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.rpc.withService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.uuid.Uuid

class Database(
    val eventLogRepo: BaseEventLogRepo,
    val outgoingEventLogRepo: OutgoingEventLogRepo,
    val repoResolve: RepositoryProvider
) : QueryExecutor, QueryExecutor.Transactional, HookApi {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val localDb: Deferred<Driver> = coroutineScope.async {
        val db = SQLite("db/local-$authorId-$localDbCounter.db")
        db.migrate(
            "../shared/migrations",
            "_sqlx4k_migrations"
        ).getOrThrow()
        db.applyInvalidationHandler(coroutineScope)
        db
    }
    private val baseDb: Deferred<Driver> = coroutineScope.async {
        val db = SQLite("db/base-$authorId.db")
        db.migrate(
            "../shared/migrations",
            "_sqlx4k_migrations",
        ).getOrThrow()
        db.applyInvalidationHandler(coroutineScope)
        db
    }
    val authorId = Uuid.random()
    var localDbCounter = 0

    lateinit var syncClient: KSyncClient<RepositoryEventSource>

    @OptIn(ExperimentalSerializationApi::class)
    val rpc = HttpClient(CIO) {

        install(WebSockets)
        install(Krpc) {
            serialization {
                cbor()
            }
        }
    }.rpc {
        url("ws://localhost:8080")
    }

    val syncApi = rpc.withService<SyncApi>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    init {
        coroutineScope.launch {
            val base = baseDb.await()
            val local = localDb.await()
            val lastConfirmedTransactionId = eventLogRepo.findOneByLast(base).getOrThrow()?.sequence ?: 0
            val initialUnconfirmedTransactions =
                outgoingEventLogRepo.findAllByNotConfirmed(local).getOrThrow().map { it.event() }
            syncClient = KSyncClient(
                RepositoryEventSource::class,
                authorId,
                lastConfirmedTransactionId,
                initialUnconfirmedTransactions,
                local,
                coroutineScope,
                repoResolve,
                {
                    if (isConnected.value)
                        syncApi.appendEvent(it)
                },
                {
                    error("Rebase not implemented")
                    val newFileName = "local-$authorId-${++localDbCounter}.db"
                    eventLogRepo.executeVacuum(base, newFileName)
                    val newLocalDb = SQLite(newFileName)
                },
                { transaction ->
                    outgoingEventLogRepo.insert(this, OutgoingEventLog(transaction))
                }
            )

            @OptIn(ExperimentalAtomicApi::class)
            while (isActive) {
                try {
                    _isConnected.value = false
                    syncApi.catchupEvents(syncClient.lastConfirmedTransactionId.load()).collect {
                        syncClient.processIncomingTransaction(it)
                    }
                    _isConnected.value = true
                    syncClient.catchup()

                    syncApi.receiveEvents().collect { event ->
                        syncClient.processIncomingTransaction(event)
                    }
                } catch (e: Exception) {
                    _isConnected.value = false
                    delay(5000)
                }
            }
        }
    }


    override val encoders: Statement.ValueEncoderRegistry
        get() = runBlocking { localDb.await().encoders }
    override val metadata: MetadataStorage
        get() = runBlocking { localDb.await().metadata }
    override val hook: HookEventBus
        get() = runBlocking { localDb.await().hook }

    override suspend fun execute(sql: String): Result<Long> {
        return localDb.await().execute(sql)
    }

    override suspend fun fetchAll(sql: String): Result<ResultSet> {
        return localDb.await().fetchAll(sql)
    }

    override suspend fun begin(): Result<Transaction> {
        return localDb.await().begin()
    }

}