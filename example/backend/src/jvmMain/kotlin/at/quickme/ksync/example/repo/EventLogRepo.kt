package at.quickme.ksync.example.repo

import at.quickme.ksync.example.entity.EventLog
import at.quickme.ksync.example.entity.EventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(EventLogMapper::class)
interface EventLogRepo : CrudRepository<EventLog> {
    @Query("select * from server_event_log where 1 = :p1 order by sequence desc limit 1")
    suspend fun findOneByLast(context: QueryExecutor, p1: Int = 1): Result<EventLog?>

    @Query("select * from server_event_log where sequence > :sequence")
    suspend fun findAllBySequence(context: QueryExecutor, sequence: Long): Result<List<EventLog>>

    @Query("select count(*) from server_event_log where author_sequence = :authorSequence and author = :author")
    suspend fun countByAuthorSequenceAndAuthor(context: QueryExecutor, authorSequence: Long, author: String): Result<Long>
}