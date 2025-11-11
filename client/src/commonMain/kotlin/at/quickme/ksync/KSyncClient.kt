package at.quickme.ksync

import TransactionEventMetadata
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.Hooks
import io.github.smyrgeorge.sqlx4k.Transaction
import io.github.smyrgeorge.sqlx4k.impl.hook.subscribeAsync
import io.github.smyrgeorge.sqlx4k.impl.metadata.MetadataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@OptIn(ExperimentalAtomicApi::class)
class KSyncClient<T : RepositoryEventBase>(
    val eventBase: KClass<T>,
    private val authorId: Uuid,
    lastConfirmedTransactionId: Long,
    initialUnconfirmedTransactions: List<EventTransaction<T>>,
    private val localDb: Driver,
    private val coroutineScope: CoroutineScope,
    private val repositoryProvider: RepositoryProvider,
    private val onSendEvent: suspend (event: EventTransaction<T>) -> Unit,
    private val onRebase: suspend () -> Unit,
    private val onTransactionCommited: suspend Transaction.(event: EventTransaction<T>) -> Unit
) {

    val lastConfirmedTransactionId = AtomicLong(lastConfirmedTransactionId)
    private val transactionQueueMutex = Mutex()
    private val unconfirmedTransactionQueue = initialUnconfirmedTransactions.sortedBy { it.sequence }.toMutableList()

    val nextUnconfirmedTransactionId: Long
        get() =
            unconfirmedTransactionQueue.lastOrNull()?.sequence?.let { it + 1 } ?: (lastConfirmedTransactionId.load() + 1)

    val nextAuthorId = AtomicLong(lastConfirmedTransactionId + 1)

    init {
        // check unconfirmed sequence
        if (unconfirmedTransactionQueue.windowed(2).any { it[0].sequence + 1 != it[1].sequence })
            error("Unconfirmed transactions are not contiguous. Make sure no transaction is missing.")
        coroutineScope.launch {
            for (transaction in unconfirmedTransactionQueue)
                onSendEvent(transaction)
        }

        localDb.hook.subscribeAsync(coroutineScope, Hooks.AfterBeginTransaction::class) {
            it.result.getOrNull()?.metadata?.apply {
                set(TransactionEventMetadata<T>())
            }
        }


        localDb.hook.subscribeAsync(coroutineScope, EventHook::class) {
            when (it.source) {
                is Transaction -> {
                    if (eventBase.isInstance(it.event)) {
                        val transactionMetadata = it.source.metadata.requireTransactionMetadata()
                        transactionMetadata.occurredEvents.add(it.event as T)
                    }
                }

                else -> {
                    error("Repository call must be made within a transaction. Otherwise consistency cannot be guaranteed.")
                }
            }
        }

        localDb.hook.subscribeAsync(coroutineScope, Transaction.BeforeCommitHook::class) {
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            if (transactionMetadata.occurredEvents.isEmpty())
                return@subscribeAsync

            val event = EventTransaction(
                events = transactionMetadata.occurredEvents.toList(),
                author = authorId,
                authorSequence = nextAuthorId.addAndFetch(1)
            )
            transactionMetadata.eventTransaction = event
            onTransactionCommited(it.source, event)
        }
        localDb.hook.subscribeAsync(coroutineScope, Transaction.AfterCommitHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            transactionMetadata.eventTransaction?.run {
                emitTransaction(this)
                transactionMetadata.occurredEvents.clear()
            }
        }

        localDb.hook.subscribeAsync(coroutineScope, Transaction.AfterRollbackHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            transactionMetadata.occurredEvents.clear()
        }
    }

    suspend fun catchup(){
        coroutineScope.launch {
            transactionQueueMutex.withLock {
                for (transaction in unconfirmedTransactionQueue)
                    onSendEvent(transaction)
            }
        }
    }

    suspend fun processIncomingTransaction(transaction: EventTransaction<T>) =
        transactionQueueMutex.withLock {
            val expectedSequenceId = lastConfirmedTransactionId.load() + 1
            if (transaction.sequence > expectedSequenceId)
                error("Expected incoming sequenceId $expectedSequenceId, but got ${transaction.sequence}.")
            else if (transaction.sequence < expectedSequenceId) {
                println("Skipping old transaction with ${transaction.sequence}, next would be ${expectedSequenceId}")
                return@withLock
            }
            lastConfirmedTransactionId.compareAndExchange(expectedSequenceId - 1, transaction.sequence)
            when {
                unconfirmedTransactionQueue.isEmpty() -> {
                    localDb.transaction {
                        transaction.execute(this, repositoryProvider)
                        metadata.requireTransactionMetadata().occurredEvents.clear()
                    }
                    println("Got transaction $lastConfirmedTransactionId")
                }

                transaction.sequence == unconfirmedTransactionQueue.first().sequence
                        && transaction.author == authorId -> {
                    unconfirmedTransactionQueue.removeFirst()
                    println("Last transaction confirmed")
                }

                else -> onRebase()
            }
        }

    private suspend fun emitTransaction(transaction: EventTransaction<T>) {
        val sequencedTransaction = transactionQueueMutex.withLock {
            val element = transaction.copy(sequence = nextUnconfirmedTransactionId)
            unconfirmedTransactionQueue.add(element)
            element
        }
        try {
            println("Sending transaction ${sequencedTransaction.sequence}")
            onSendEvent(sequencedTransaction)
        } catch (e: Throwable) {
            print(e)
        }
    }


    private fun MetadataStorage.requireTransactionMetadata() = get<TransactionEventMetadata<T>>()
        ?: error("Transaction without ${TransactionEventMetadata::class.simpleName} metadata found. Have you canceled the coroutine scope of ${KSyncClient::class.simpleName}?: ${this::class}")

}