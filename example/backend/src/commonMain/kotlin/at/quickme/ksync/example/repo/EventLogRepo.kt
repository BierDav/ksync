package at.quickme.ksync.example.repo

import at.quickme.ksync.example.entity.EventLog
import at.quickme.ksync.example.entity.EventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository
import kotlin.uuid.Uuid

@Repository(EventLogMapper::class)
interface EventLogRepo : CrudRepository<EventLog> {
    @Query("select * from event_log order by sequence desc limit 1")
    suspend fun findOneByLast(context: QueryExecutor): Result<EventLog>

    @Query("select count(*) from event_log where author_sequence = :authorSequence and author = :author")
    suspend fun countByAuthorSequenceAndAuthor(context: QueryExecutor, authorSequence: Long, author: Uuid): Result<Long>
}