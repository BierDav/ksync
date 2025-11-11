package at.quickme.ksync

import io.github.smyrgeorge.sqlx4k.Hooks
import io.github.smyrgeorge.sqlx4k.QueryExecutor
import kotlin.reflect.KClass

interface RepositoryEventBase {
    suspend fun run(context: QueryExecutor, repositoryProvider: RepositoryProvider): Result<*>
}


class EventHook(
    override val dependentTables: List<KClass<*>>,
    override val result: Result<Any>,
    override val source: QueryExecutor,
    val event: RepositoryEventBase,
) : Hooks.AfterCrudRepoStatement<QueryExecutor, Any>


