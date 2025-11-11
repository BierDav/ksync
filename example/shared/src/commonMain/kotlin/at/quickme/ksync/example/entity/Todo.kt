package at.quickme.ksync.example.entity

import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asBoolean
import io.github.smyrgeorge.sqlx4k.impl.extensions.asInt
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable

@Serializable
@Table("todo")
data class Todo(
    @Id val id: Int = 0,
    val task: String,
    val isDone: Boolean = false
)


object TodoMapper : RowMapper<Todo> {
    override fun map(row: ResultSet.Row): Todo =
        Todo(
            id = row.get("id").asInt(),
            task = row.get("task").asString(),
            isDone = row.get("is_done").asBoolean()
        )
}
