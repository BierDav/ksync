package io.github.smyrgeorge.sqlx4k.processor

import io.github.smyrgeorge.sqlx4k.annotation.Column
import io.github.smyrgeorge.sqlx4k.annotation.Id
import io.github.smyrgeorge.sqlx4k.annotation.Query
import io.github.smyrgeorge.sqlx4k.annotation.Repository
import io.github.smyrgeorge.sqlx4k.annotation.Table
import kotlinx.coroutines.flow.Flow

/**
 * Centralized fully qualified names used by codegen processors to avoid magic strings.
 */
object TypeNames {
    // Annotations
    val REPOSITORY_ANNOTATION = Repository::class.qualifiedName!!
    val TABLE_ANNOTATION = Table::class.qualifiedName!!
    val ID_ANNOTATION = Id::class.qualifiedName!!
    val COLUMN_ANNOTATION = Column::class.qualifiedName!!
    val QUERY_ANNOTATION = Query::class.qualifiedName!!

    // Core interfaces/classes
    const val CRUD_REPOSITORY = "io.github.smyrgeorge.sqlx4k.CrudRepository"
    const val CONTEXT_CRUD_REPOSITORY = "io.github.smyrgeorge.sqlx4k.ContextCrudRepository"
    const val QUERY_EXECUTOR = "io.github.smyrgeorge.sqlx4k.QueryExecutor"
    const val LISTEN_FOR_INVALIDATION = "io.github.smyrgeorge.sqlx4k.invalidation.listenForInvalidation"
    const val HOOK_API = "io.github.smyrgeorge.sqlx4k.impl.hook.HookApi"
    const val HOOKS = "io.github.smyrgeorge.sqlx4k.Hooks"
    const val MUTABLE_HOOK_EVENT_BUS = "io.github.smyrgeorge.sqlx4k.impl.hook.MutableHookEventBus"
    const val STATEMENT = "io.github.smyrgeorge.sqlx4k.Statement"

    // Kotlin stdlib
    val KOTLIN_RESULT = Result::class.qualifiedName!!
    val KOTLIN_LIST = List::class.qualifiedName!!
    val KOTLIN_LONG = Long::class.qualifiedName!!
    val KOTLIN_INT = Int::class.qualifiedName!!
    val KOTLIN_FLOW = Flow::class.qualifiedName!!
}
