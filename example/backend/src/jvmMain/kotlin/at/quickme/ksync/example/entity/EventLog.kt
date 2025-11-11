package at.quickme.ksync.example.entity

import at.quickme.ksync.EventTransaction
import at.quickme.ksync.example.codegen.RepositoryEventSource
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
@Table("server_event_log")
data class EventLog(
    @Id val sequence: Long = 0,
    val eventsSerialized: String,
    val author: Uuid,
    val authorSequence: Long,
) {
    constructor(event: EventTransaction<RepositoryEventSource>) :
            this(
                eventsSerialized = Json.encodeToString(event.events),
                author = event.author,
                authorSequence = event.authorSequence
            )

    fun events() = Json.decodeFromString<List<RepositoryEventSource>>(eventsSerialized)
    fun event() = EventTransaction(sequence, events(), author, authorSequence)
}

object EventLogMapper : RowMapper<EventLog> {
    override fun map(row: ResultSet.Row): EventLog {
        return EventLog(
            sequence = row.get("sequence").asLong(),
            eventsSerialized = row.get("events_serialized").asString(),
            author = row.get("author").asUuid(),
            authorSequence = row.get("author_sequence").asLong(),
        )
    }
}