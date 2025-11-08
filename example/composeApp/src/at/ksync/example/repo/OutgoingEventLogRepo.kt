package repo

import at.ksync.example.entity.OutgoingEventLog
import at.ksync.example.entity.OutgoingEventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(OutgoingEventLogMapper::class)
interface OutgoingEventLogRepo : CrudRepository<OutgoingEventLog>