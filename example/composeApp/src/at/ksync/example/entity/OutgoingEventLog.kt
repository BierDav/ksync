package at.ksync.example.entity

import at.quickme.ksync.EventTransaction
import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Table("event_transaction_log")
data class OutgoingEventLog(
    @Id val sequence: Long,
    val eventSerialized: String
) {
    constructor(sequence: Long, event: EventTransaction) :
            this(sequence, Json.encodeToString(event))
    val event by lazy { Json.decodeFromString<EventTransaction>(eventSerialized) }
}

class OutgoingEventLogMapper : RowMapper<OutgoingEventLog> {
    override fun map(row: ResultSet.Row): OutgoingEventLog {
        return OutgoingEventLog(
            sequence = row.get(OutgoingEventLog::sequence.name).asLong(),
            eventSerialized = row.get(OutgoingEventLog::event.name).asString()
        )
    }
}