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
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

@OptIn(ExperimentalAtomicApi::class)
class KSyncClient<T : RepositoryEventBase>(
    val eventBase: KClass<T>,
    private val authorId: Uuid,
    var lastConfirmedTransactionId: Long,
    initialUnconfirmedTransactions: List<EventTransaction<T>>,
    private var driver: Driver,
    private val coroutineScope: CoroutineScope,
    private val repositoryProvider: RepositoryProvider,
    private val onSendEvent: suspend (event: EventTransaction<T>) -> Unit,
    private val onRebase: suspend () -> Driver,
    private val beforeTransactionCommit: suspend Transaction.(event: EventTransaction<T>) -> EventTransaction<T>
) {

    private val transactionQueueMutex = Mutex()

    private val unconfirmedTransactionQueue = initialUnconfirmedTransactions.sortedBy { it.sequence }.toMutableList()
    val nextUnconfirmedTransactionId: Long
        get() =
            unconfirmedTransactionQueue.lastOrNull()?.sequence?.let { it + 1 }
                ?: (lastConfirmedTransactionId + 1)
    var currentDbId = initialUnconfirmedTransactions.maxOfOrNull { it.sequence } ?: lastConfirmedTransactionId


    init {
        // check unconfirmed sequence
        if (unconfirmedTransactionQueue.windowed(2).any { it[0].sequence + 1 != it[1].sequence })
            error("Unconfirmed transactions are not contiguous. Make sure no transaction is missing.")
        coroutineScope.launch {
            for (transaction in unconfirmedTransactionQueue)
                onSendEvent(transaction)
        }
        swapDriver(driver)
    }


    private fun swapDriver(newDriver: Driver) {
        driver = newDriver
        driver.hook.subscribeAsync(coroutineScope, Hooks.AfterBeginTransaction::class) {
            it.result.getOrNull()?.metadata?.apply {
                set(TransactionEventMetadata<T>())
            }
        }


        driver.hook.subscribeAsync(coroutineScope, EventHook::class) {
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

        driver.hook.subscribeAsync(coroutineScope, Transaction.BeforeCommitHook::class) {
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            if (transactionMetadata.occurredEvents.isEmpty())
                return@subscribeAsync

            val event = EventTransaction(
                sequence = currentDbId + 1,
                events = transactionMetadata.occurredEvents.toList(),
                author = authorId,
            )
            transactionMetadata.eventTransaction = beforeTransactionCommit(it.source, event)
        }
        driver.hook.subscribeAsync(coroutineScope, Transaction.AfterCommitHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            transactionMetadata.occurredEvents.clear()
            transactionMetadata.eventTransaction?.run { transactionCommited(this) }
            transactionMetadata.eventTransaction = null
        }

        driver.hook.subscribeAsync(coroutineScope, Transaction.AfterRollbackHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            transactionMetadata.occurredEvents.clear()
            transactionMetadata.eventTransaction = null
        }
    }

    suspend fun catchup() {
        coroutineScope.launch {
            transactionQueueMutex.withLock {
                for (transaction in unconfirmedTransactionQueue)
                    onSendEvent(transaction)
            }
        }
    }

    suspend fun processIncomingTransaction(transaction: EventTransaction<T>) =
        transactionQueueMutex.withLock {
            val expectedSequenceId = lastConfirmedTransactionId + 1
            if (transaction.sequence > expectedSequenceId)
                error("Expected incoming sequenceId $expectedSequenceId, but got ${transaction.sequence}.")
            else if (transaction.sequence < expectedSequenceId) {
                println("Skipping old transaction with ${transaction.sequence}, next would be ${expectedSequenceId}")
                return@withLock
            }
            when {
                unconfirmedTransactionQueue.isNotEmpty()
                        && transaction.authorSequence == unconfirmedTransactionQueue.first().authorSequence
                        && transaction.author == authorId -> {
                    if (currentDbId < transaction.sequence) {
                        driver.transaction {
                            transaction.execute(this, repositoryProvider)
                            beforeTransactionCommit(this, transaction)
                            metadata.requireTransactionMetadata().occurredEvents.clear()
                        }
                        currentDbId = transaction.sequence
                    }
                    unconfirmedTransactionQueue.removeFirst()
                    println("Last transaction confirmed: ${transaction.sequence}")
                }

                lastConfirmedTransactionId == currentDbId -> {
                    driver.transaction {
                        transaction.execute(this, repositoryProvider)
                        beforeTransactionCommit(this, transaction)
                        metadata.requireTransactionMetadata().occurredEvents.clear()
                    }
                    currentDbId = transaction.sequence
                    println("Got transaction $lastConfirmedTransactionId")
                }


                else -> {
                    val driver = onRebase()
                    swapDriver(driver)
                    driver.transaction {
                        transaction.execute(this, repositoryProvider)
                        beforeTransactionCommit(this, transaction)
                        metadata.requireTransactionMetadata().occurredEvents.clear()
                    }
                    currentDbId = transaction.sequence
                    println("Rebased and applied transaction $lastConfirmedTransactionId")
                }
            }
            lastConfirmedTransactionId = transaction.sequence
        }

    private suspend fun transactionCommited(transaction: EventTransaction<T>) {
        transactionQueueMutex.withLock {
            currentDbId = transaction.sequence
            unconfirmedTransactionQueue.add(transaction)
        }
        try {
            println("Sending transaction ${transaction.sequence}")
            onSendEvent(transaction)
        } catch (e: Throwable) {
            print(e)
        }
    }


    private fun MetadataStorage.requireTransactionMetadata() = get<TransactionEventMetadata<T>>()
        ?: error("Transaction without ${TransactionEventMetadata::class.simpleName} metadata found. Have you canceled the coroutine scope of ${KSyncClient::class.simpleName}?: ${this::class}")

}