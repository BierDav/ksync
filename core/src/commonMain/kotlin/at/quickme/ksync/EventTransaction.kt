package at.quickme.ksync

import io.github.smyrgeorge.sqlx4k.Transaction
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

interface RepositoryProvider {
    fun <T : Any> resolve(clazz: KClass<T>): T
}

@Serializable
data class EventTransaction<T : RepositoryEventBase>(
    val sequence: Long = 0,
    val events: List<T>,
    val author: Uuid,
    val authorSequence: Long = sequence,
) {
    suspend fun execute(transaction: Transaction, provider: RepositoryProvider) {
        events.forEach {
            it.run(transaction, provider)
        }
    }
}

