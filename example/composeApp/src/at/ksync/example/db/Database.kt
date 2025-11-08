package db

import KSyncClient
import io.github.smyrgeorge.sqlx4k.Driver
import io.github.smyrgeorge.sqlx4k.sqlite.SQLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import repo.BaseEventLogRepo
import kotlin.uuid.Uuid

class Database(
    val eventLogRepo: BaseEventLogRepo,
) {
    val baseDb: Driver
    val localDb: Driver

    lateinit var syncClient: KSyncClient

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        baseDb = SQLite("base.db")
        localDb = SQLite("local.db")

//        coroutineScope.launch {
//            val lastConfirmedTransactionId = BaseEventLogRepo
//            syncClient = KSyncClient(
//                Uuid.random(),
//
//                )
//        }

    }
}