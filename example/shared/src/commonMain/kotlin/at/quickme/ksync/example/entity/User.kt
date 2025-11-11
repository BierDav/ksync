package at.quickme.ksync.example.entity

import io.github.smyrgeorge.sqlx4k.ResultSet
import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Table
import io.github.smyrgeorge.sqlx4k.impl.extensions.asLong
import kotlinx.serialization.Serializable

@Serializable
@Table("user")
class User(
    @Id val id: Long,
    val name: String)

object UserMapper: RowMapper<User> {
    override fun map(row: ResultSet.Row): User =
        User(
            row.get("id").asLong(),
            row.get("name").asString()
        )
}