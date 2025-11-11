package at.quickme.ksync.example.api

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.example.Database
import at.quickme.ksync.example.codegen.RepositoryEventSource
import at.quickme.ksync.example.repo.EventLogRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SyncApiImpl(
    private val database: Database,
    private val eventLogRepo: EventLogRepo
) : SyncApi {
    override suspend fun hello(): String {
        return "Hello World"
    }

    override fun catchupEvents(lastSequence: Long): Flow<EventTransaction<RepositoryEventSource>> = flow {
        eventLogRepo.findAllBySequence(database, lastSequence).getOrThrow().map { emit(it.event()) }
    }

    override suspend fun appendEvents(events: List<EventTransaction<RepositoryEventSource>>) {
        println("Appending events")
        for (event in events) {
            if (eventLogRepo.countByAuthorSequenceAndAuthor(
                    database,
                    event.authorSequence,
                    event.author.toString()
                ).getOrElse { 1 } > 0
            ) {
                println("dropped because of duplicate")
                continue
            }
            database.ksyncServer.await().incomingTransactions.send(event)
        }
    }

    override fun receiveEvents(): Flow<EventTransaction<RepositoryEventSource>> = flow {
        database.ksyncServer.await().receiveOutgoingTransactionsAsFlow().collect { emit(it) }
    }

}