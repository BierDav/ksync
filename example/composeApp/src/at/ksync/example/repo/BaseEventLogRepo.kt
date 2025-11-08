package repo

import at.ksync.example.entity.BaseEventLog
import at.ksync.example.entity.BaseEventLogMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(BaseEventLogMapper::class)
interface BaseEventLogRepo : CrudRepository<BaseEventLog>