package at.quickme.ksync

import io.github.smyrgeorge.sqlx4k.QueryExecutor
import io.github.smyrgeorge.sqlx4k.Transaction
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

interface RepositoryProvider {
    fun <T : Any> get(clazz: KClass<T>): T
}

@Serializable
data class EventTransaction(
    val sequence: Long,
    val events: List<RepositoryEventBase<*>>,
    val author: Uuid,
    val authorSequence: Long = sequence,
) {
    suspend fun execute(transaction: Transaction, provider: RepositoryProvider) {
        events.forEach {
            executeEvent(it, transaction, provider)
        }
    }

    private suspend fun <T : Any> executeEvent(
        event: RepositoryEventBase<T>,
        context: QueryExecutor,
        provider: RepositoryProvider,
    ) {
        val repo = provider.get(event.repoType)
        when (event) {
            is RepositoryEventSourceMarker -> event.run(context, repo)
            is ContextRepositoryEventMarker -> with(context) { event.run(repo) }
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}

