package at.quickme.ksync.at.quickme.ksync.example.repo

import at.quickme.ksync.at.quickme.ksync.example.entity.Todo
import at.quickme.ksync.at.quickme.ksync.example.entity.TodoMapper
import io.github.smyrgeorge.sqlx4k.CrudRepository
import io.github.smyrgeorge.sqlx4k.annotation.Repository

@Repository(TodoMapper::class)
interface TodoRepo : CrudRepository<Todo>