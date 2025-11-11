package at.quickme.ksync.api

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.RepositoryEventBase
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

interface SyncApiBase<T : RepositoryEventBase> {
    suspend fun appendEvent(event: EventTransaction<T>) = appendEvents(listOf(event))
    suspend fun appendEvents(events: List<EventTransaction<T>>)
    fun receiveEvents(): Flow<EventTransaction<T>>
}