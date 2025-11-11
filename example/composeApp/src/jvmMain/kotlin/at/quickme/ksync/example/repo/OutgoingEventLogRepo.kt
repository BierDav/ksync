package at.quickme.ksync.example.repo

import at.quickme.ksync.example.entity.OutgoingEventLog
import at.quickme.ksync.example.entity.OutgoingEventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(OutgoingEventLogMapper::class)
interface OutgoingEventLogRepo : CrudRepository<OutgoingEventLog>{
    @Query("select o.sequence as sequence, event_serialized from outgoing_event_log o left join base_event_log b on o.sequence = b.sequence where b.sequence is null and 1 = :p1")
    suspend fun findAllByNotConfirmed(context: QueryExecutor, p1:Int = 1): Result<List<OutgoingEventLog>>
}