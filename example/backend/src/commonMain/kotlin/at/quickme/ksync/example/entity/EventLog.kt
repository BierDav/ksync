package at.quickme.ksync.example.entity

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.RepositoryEventBase
import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import io.github.smyrgeorge.sqlx4k.impl.extensions.asUuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@Serializable
@Table("outgoing_event_log")
data class EventLog(
    @Id val sequence: Long = 0,
    val eventsSerialized: String,
    val author: Uuid,
    val authorSequence: Long,
) {
    constructor(event: EventTransaction) :
            this(event.sequence,
                Json.encodeToString(event.events),
                event.author,
                event.authorSequence)

    val events by lazy { Json.decodeFromString<List<RepositoryEventBase<*>>>(eventsSerialized) }
}

object EventLogMapper : RowMapper<EventLog> {
    override fun map(row: ResultSet.Row): EventLog {
        return EventLog(
            sequence = row.get(EventLog::sequence.name).asLong(),
            eventsSerialized = row.get(EventLog::eventsSerialized.name).asString(),
            author = row.get(EventLog::author.name).asUuid(),
            authorSequence = row.get(EventLog::authorSequence.name).asLong(),
        )
    }
}