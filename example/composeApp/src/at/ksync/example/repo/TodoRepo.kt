package at.ksync.example.repo

import at.ksync.example.entity.Todo
import at.ksync.example.entity.TodoMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(TodoMapper::class)
interface TodoRepo : CrudRepository<Todo>