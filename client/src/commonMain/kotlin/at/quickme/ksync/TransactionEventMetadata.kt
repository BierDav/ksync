import at.quickme.ksync.EventTransaction
import at.quickme.ksync.RepositoryEventBase

class TransactionEventMetadata<T : RepositoryEventBase>(
    val occurredEvents: MutableList<T> = mutableListOf(),
    var eventTransaction: EventTransaction<T>? = null
)