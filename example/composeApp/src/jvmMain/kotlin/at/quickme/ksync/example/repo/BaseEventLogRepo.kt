package at.quickme.ksync.example.repo

import at.quickme.ksync.example.entity.BaseEventLog
import at.quickme.ksync.example.entity.BaseEventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(BaseEventLogMapper::class)
interface BaseEventLogRepo : CrudRepository<BaseEventLog> {
    @Query("select * from base_event_log where 1 = :p1 order by sequence desc limit 1")
    suspend fun findOneByLast(context: QueryExecutor, p1: Int = 1): Result<BaseEventLog?>

    @Query("VACUUM main INTO :targetFilename", checkSyntax = false)
    suspend fun executeVacuum(context: QueryExecutor, targetFilename: String): Result<Long>
}