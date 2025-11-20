package at.quickme.ksync.example.db

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.KSyncClient
import at.quickme.ksync.RepositoryProvider
import at.quickme.ksync.example.api.SyncApi
import at.quickme.ksync.example.codegen.RepositoryEventSource
import at.quickme.ksync.example.entity.BaseEventLog
import at.quickme.ksync.example.entity.OutgoingEventLog
import at.quickme.ksync.example.repo.BaseEventLogRepo
import at.quickme.ksync.example.repo.OutgoingEventLogRepo
import io.github.smyrgeorge.sqlx4k.*
import io.github.smyrgeorge.sqlx4k.impl.hook.HookApi
import io.github.smyrgeorge.sqlx4k.impl.hook.HookEventBus
import io.github.smyrgeorge.sqlx4k.impl.metadata.MetadataStorage
import io.github.smyrgeorge.sqlx4k.invalidation.InvalidationScope
import io.github.smyrgeorge.sqlx4k.invalidation.applyInvalidationHandler
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.rpc.withService
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.uuid.Uuid

class Database(
    val baseEventLogRepo: BaseEventLogRepo,
    val outgoingEventLogRepo: OutgoingEventLogRepo,
    val repoResolve: RepositoryProvider
) : QueryExecutor, QueryExecutor.Transactional, HookApi {

    val invalidationScope = InvalidationScope()
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val authorId = Uuid.random()
    var localDbCounter = 0


    val localDbFilename
        get() = "db/local-$authorId-$localDbCounter.db";
    private var localDb: Deferred<Driver> = coroutineScope.async {
        val db = SQLite("db/local-$authorId-$localDbCounter.db")
        db.migrate(
            "../shared/migrations",
            "_sqlx4k_migrations"
        ).getOrThrow()
        db.applyInvalidationHandler(coroutineScope, invalidationScope)
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


    lateinit var syncClient: KSyncClient<RepositoryEventSource>

    @OptIn(ExperimentalSerializationApi::class)
    val client = HttpClient(CIO) {
        install(WebSockets)
        defaultRequest {
            url {
                protocol = URLProtocol.WS
                host = "localhost"
                port = 8080
            }
        }
        install(Krpc) {
            serialization {
                cbor()
            }
        }
    }

    var syncRpcClient: KtorRpcClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    init {

        coroutineScope.launch {
            val base = baseDb.await()
            val local = localDb.await()
            val lastConfirmedTransactionId = baseEventLogRepo.findOneByLast(base).getOrThrow()?.sequence ?: 0
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
                    runCatching {
                        syncRpcClient?.withService<SyncApi>()?.appendEvent(it)
                    }.getOrElse {
                        println("error when sending event: $it")
                    }
                },
                { doRebase(base) },
                { transaction ->
                    outgoingEventLogRepo.insert(this, OutgoingEventLog(transaction)).getOrThrow().event()
                }
            )

            connect()
        }
    }

    val rebaseMutex = Mutex()

    private suspend fun doRebase(base: Driver): Driver = rebaseMutex.withLock {
        val before = localDb.await()
        val beforeFilename = localDbFilename
        localDbCounter++
        val newFilename = localDbFilename
        if (File(newFilename).exists())
            File(newFilename).delete()
        localDb = coroutineScope.async {
            base.transaction {
                baseEventLogRepo.executeVacuum(base, newFilename)
                val db = SQLite(newFilename)
                db.applyInvalidationHandler(coroutineScope, invalidationScope)
                db
            }
        }
        try {

            return localDb.await().apply {
                before.close()
                File(beforeFilename).delete()
                invalidationScope.invalidate(listOf(Any::class))
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun connect() {
        if (_isConnected.value)
            return
        _isConnected.value = true
        coroutineScope.launch {
            try {
                val rpc = client.rpc {
                    url("ws://localhost:8080")
                }
                syncRpcClient = rpc
                val api = rpc.withService<SyncApi>()
                api.catchupEvents(syncClient.lastConfirmedTransactionId).collect {
                    processIncomingTransaction(it)
                }
                syncClient.catchup()
                api.receiveEvents().collect { event ->
                    processIncomingTransaction(event)
                }
            } catch (e: Exception) {
                println("connection exited: $e")
            } finally {
                _isConnected.value = false
                syncRpcClient = null
            }
        }
    }

    suspend fun processIncomingTransaction(transaction: EventTransaction<RepositoryEventSource>) {
        rebaseMutex.withLock {
            baseDb.await().transaction {
                val event = baseEventLogRepo.findOneByLast(this).getOrThrow()
                val expectedSequence = event?.sequence?.let { it + 1 } ?: 1
                when {
                    event == null || transaction.sequence == expectedSequence -> {
                        transaction.execute(this, repoResolve)
                        baseEventLogRepo.insert(this, BaseEventLog(expectedSequence))
                    }

                    transaction.sequence > expectedSequence ->
                        error("BaseDb: Expected incoming sequenceId $expectedSequence, but got ${transaction.sequence}.")

                    else -> {
                        println("dropped base transaction, because it was already applied: ${transaction.sequence}")
                    }
                }
            }
        }
        syncClient.processIncomingTransaction(transaction)
    }

    fun disconnect() {
        coroutineScope.launch {
            syncRpcClient?.webSocketSession?.await()?.close()
        }
    }


    override suspend fun begin(): Result<Transaction> =
        localDb.await().begin()

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
}