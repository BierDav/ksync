package at.quickme.ksync.example.entity

import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable

@Serializable
@Table("base_event_log")
data class BaseEventLog(
    @Id val sequence: Long,
)

object BaseEventLogMapper : RowMapper<BaseEventLog> {
    override fun map(row: ResultSet.Row) =
        BaseEventLog(row.get("sequence").asLong())
}