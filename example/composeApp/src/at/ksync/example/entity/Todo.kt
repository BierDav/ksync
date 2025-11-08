package at.ksync.example.entity

import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asBoolean
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable

@Serializable
@Table("todo")
data class Todo(
    @Id val id: Long = 0,
    val task: String,
    val isDone: Boolean = false
)


class TodoMapper : RowMapper<Todo> {
    override fun map(row: ResultSet.Row): Todo =
        Todo(
            id = row.get("id").asLong(),
            task = row.get("task").asString(),
            isDone = row.get("is_done").asBoolean()
        )

}


fun fewaf(){
   val test = Result.success(emptyList<Unit>()).run {
        map { list -> list.firstOrNull() ?: return@run Result.failure(IllegalStateException("Update query returned no rows")) }
    }
}