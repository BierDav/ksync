package at.ksync.example.entity

import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable

@Serializable
@Table("event_transaction_log")
data class BaseEventLog(
    @Id val sequence: Long,
)

class BaseEventLogMapper : RowMapper<BaseEventLog> {
    override fun map(row: ResultSet.Row) =
        BaseEventLog(row.get("sequence").asLong())
}