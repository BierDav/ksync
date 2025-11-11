package at.quickme.ksync.example.repo

import at.quickme.ksync.example.entity.Todo
import at.quickme.ksync.example.entity.TodoMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository
import kotlinx.coroutines.flow.Flow

@Repository(TodoMapper::class)
interface TodoRepo : CrudRepository<Todo>{
    @Query("select * from todo")
    fun findAllFlow(context: QueryExecutor): Flow<Result<List<Todo>>>
}