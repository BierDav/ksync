import at.quickme.ksync.EventTransaction
import at.quickme.ksync.RepositoryProvider
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class KSyncServer(
    private val localDb: Driver,
    private var lastTransactionId: Long,
    private val coroutineScope: CoroutineScope,
    private val repositoryProvider: RepositoryProvider,
    private val onTransactionCommited: suspend Transaction.(event: EventTransaction) -> Unit,
    private val transactionBufferCapacity: Int = 1000,
) {
    /***
     * Send all unique incoming transactions from the clients to this channel.
     *
     * Be careful to make sure that no transaction with the same [AuthoredEventTransaction.author] and [AuthoredEventTransaction.sequence]
     * already exists in the database otherwise this might cause infinite loops.
     */
    val incomingTransactions = Channel<EventTransaction>(transactionBufferCapacity)
    private val outgoingTransactionFlow = MutableSharedFlow<EventTransaction>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    init {
        coroutineScope.run {
            launch {
                for (transaction in incomingTransactions) {
                    if(transaction.events.isEmpty()) continue
                    val event = transaction.copy(sequence = ++lastTransactionId)
                    localDb.transaction {
                        transaction.execute(this, repositoryProvider)
                        onTransactionCommited(this,event)
                    }
                    outgoingTransactionFlow.emit(event)
                }
            }
        }
    }

    suspend fun receiveOutgoingTransactionsAsFlow(): Flow<EventTransaction> =
        channelFlow { // TODO: set capacity to transactionBufferCapacity
            launch {
                outgoingTransactionFlow.collect { item ->
                    trySend(item).getOrElse {
                        close(Error("Private buffer of size $channel overflowed."))
                    }
                }
            }
        }
}