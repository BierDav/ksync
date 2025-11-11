package at.quickme.ksync.example.api

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.api.SyncApiBase
import at.quickme.ksync.example.codegen.RepositoryEventSource
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface SyncApi : SyncApiBase<RepositoryEventSource>{
    suspend fun hello(): String
    fun catchupEvents(lastSequence: Long): Flow<EventTransaction<RepositoryEventSource>>
    override suspend fun appendEvents(events: List<EventTransaction<RepositoryEventSource>>)
    override fun receiveEvents(): Flow<EventTransaction<RepositoryEventSource>>
}