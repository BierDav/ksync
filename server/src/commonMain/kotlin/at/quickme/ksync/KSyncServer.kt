package at.quickme.ksync

import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class KSyncServer<T : RepositoryEventBase>(
    private val localDb: Driver,
    private var lastTransactionId: Long,
    private val coroutineScope: CoroutineScope,
    private val repositoryProvider: RepositoryProvider,
    private val onTransactionCommited: suspend Transaction.(event: EventTransaction<T>) -> EventTransaction<T>,
    private val transactionBufferCapacity: Int = 1000,
) {
    /***
     * Send all unique incoming transactions from the clients to this channel.
     *
     * Be careful to make sure that no transaction with the same [AuthoredEventTransaction.author] and [AuthoredEventTransaction.sequence]
     * already exists in the database otherwise this might cause infinite loops.
     */
    val incomingTransactions = Channel<EventTransaction<T>>(transactionBufferCapacity)
    private val outgoingTransactionFlow = MutableSharedFlow<EventTransaction<T>>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    init {
        coroutineScope.run {
            launch {
                for (transaction in incomingTransactions) {
                    if (transaction.events.isEmpty())
                        continue
                    val event = transaction.copy(sequence = ++lastTransactionId)
                    val savedEvent = localDb.transaction {
                        transaction.execute(this, repositoryProvider)
                        onTransactionCommited(this, event)
                    }
                    lastTransactionId = savedEvent.sequence
                    outgoingTransactionFlow.emit(savedEvent)
                }
            }
        }
    }

    fun receiveOutgoingTransactionsAsFlow(): Flow<EventTransaction<T>> =
        channelFlow { // TODO: set capacity to transactionBufferCapacity
            launch {
                outgoingTransactionFlow.collect { item ->
                    println("sent")
                    trySend(item).getOrElse {
                        close(Error("Private buffer of size $channel overflowed."))
                    }
                }
            }
        }
}