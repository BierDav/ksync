import at.quickme.ksync.EventHook
import at.quickme.ksync.EventTransaction
import at.quickme.ksync.RepositoryProvider
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.Hooks
import io.github.smyrgeorge.sqlx4k.Transaction
import io.github.smyrgeorge.sqlx4k.impl.hook.subscribeAsync
import io.github.smyrgeorge.sqlx4k.impl.metadata.MetadataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class KSyncClient(
    private val authorId: Uuid,
    private var lastConfirmedTransactionId: Long,
    initialUnconfirmedTransactions: List<EventTransaction>,
    private val localDb: Driver,
    private val coroutineScope: CoroutineScope,
    private val repositoryProvider: RepositoryProvider,
    private val onRebase: suspend () -> Unit,
    private val onTransactionCommited: suspend Transaction.(event: EventTransaction) -> Unit
) {
    private val unconfirmedTransactionQueue = initialUnconfirmedTransactions.sortedBy { it.sequence }.toMutableList()
    val incomingTransactions = Channel<EventTransaction>()
    val outgoingTransactions = Channel<EventTransaction>(Channel.UNLIMITED)

    val nextUnconfirmedTransactionId: Long
        get()
        = unconfirmedTransactionQueue.last().sequence + 1

    init {
        // check unconfirmed sequence
        if (unconfirmedTransactionQueue.windowed(2).any { it[0].sequence + 1 != it[1].sequence })
            error("Unconfirmed transactions are not contiguous. Make sure no transaction is missing.")
        for (transaction in unconfirmedTransactionQueue)
            outgoingTransactions.trySend(transaction)
                .getOrElse { error("Failed to hydrate outgoingTransactions") }

        localDb.hook.subscribeAsync(coroutineScope, Hooks.AfterBeginTransaction::class) {
            it.result.getOrNull()?.metadata?.apply {
                set(TransactionEventMetadata())
            }
        }


        localDb.hook.subscribeAsync(coroutineScope, EventHook::class) {
            when (it.source) {
                is Transaction -> {
                    val transactionMetadata = it.source.metadata.requireTransactionMetadata()
                    transactionMetadata.occurredEvents.add(it.event)
                }

                else -> {
                    error("Repository call must be made within a transaction. Otherwise consistency cannot be guaranteed.")
                }
            }
        }

        localDb.hook.subscribeAsync(coroutineScope, Transaction.AfterCommitHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            if (transactionMetadata.occurredEvents.isEmpty())
                return@subscribeAsync

            val event = EventTransaction(
                nextUnconfirmedTransactionId,
                transactionMetadata.occurredEvents.toList(),
                authorId
            )
            onTransactionCommited(it.source, event)
            emitTransaction(event)
            transactionMetadata.occurredEvents.clear()
        }

        localDb.hook.subscribeAsync(coroutineScope, Transaction.AfterRollbackHook::class) {
            if (it.result.isFailure)
                return@subscribeAsync
            val transactionMetadata = it.source.metadata.requireTransactionMetadata()
            transactionMetadata.occurredEvents.clear()
        }

        coroutineScope.launch {
            for (transaction in incomingTransactions) {
                val expectedSequenceId = lastConfirmedTransactionId + 1
                if (transaction.sequence != expectedSequenceId)
                    error("Expected incoming sequenceId $expectedSequenceId, but got ${transaction.sequence}.")
                if (unconfirmedTransactionQueue.isEmpty()) {
                    localDb.transaction {
                        transaction.execute(this, repositoryProvider)
                        metadata.requireTransactionMetadata().occurredEvents.clear()
                    }
                    lastConfirmedTransactionId = transaction.sequence
                } else if (transaction == unconfirmedTransactionQueue.first())
                    unconfirmedTransactionQueue.removeFirst()
                else {
                    onRebase()
                }
            }
        }
    }

    private suspend fun emitTransaction(transaction: EventTransaction) {
        unconfirmedTransactionQueue.add(transaction)
        outgoingTransactions.send(transaction)
    }


    private fun MetadataStorage.requireTransactionMetadata() = get<TransactionEventMetadata>()
        ?: error("Transaction without ${TransactionEventMetadata::class.simpleName} metadata found. Have you canceled the coroutine scope of ${KSyncClient::class.simpleName}?: ${this::class}")

}