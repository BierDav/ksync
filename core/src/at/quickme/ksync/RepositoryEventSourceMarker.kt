package at.quickme.ksync

import io.github.smyrgeorge.sqlx4k.Hooks
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import kotlin.reflect.KClass

sealed interface RepositoryEventBase<T : Any> {
    val repoType: KClass<T>
}

interface RepositoryEventSourceMarker<T : Any> : RepositoryEventBase<T> {

    suspend fun run(context: QueryExecutor, repo: T): Result<*>
}


interface ContextRepositoryEventMarker<T : Any> : RepositoryEventBase<T> {
    context(context: QueryExecutor)
    suspend fun run(repo: T): Result<*>
}


class EventHook(
    override val dependentTables: List<KClass<*>>,
    override val result: Result<Any>,
    override val source: QueryExecutor,
    val event: RepositoryEventBase<*>,
) : Hooks.AfterCrudRepoStatement<QueryExecutor, Any>


