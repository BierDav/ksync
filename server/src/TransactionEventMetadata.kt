import at.quickme.ksync.RepositoryEventBase

class TransactionEventMetadata (val occurredEvents: MutableList<RepositoryEventBase> = mutableListOf())