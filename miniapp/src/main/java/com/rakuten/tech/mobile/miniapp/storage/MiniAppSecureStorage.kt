package com.rakuten.tech.mobile.miniapp.storage

import android.app.Activity
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.database.MAX_DB_SPACE_LIMIT_REACHED_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppDatabase
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppDatabaseImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.SQLException

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorage(
    private val databaseVersion: Int,
    private val maxDatabaseSizeInKB: Int,
    private val activity: Activity
) {

    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var miniAppDatabases: MutableMap<String, MiniAppDatabase> = HashMap()

    private fun initOrGetDB(miniAppId: String) : MiniAppDatabase {
        val maxDBSize = (maxDatabaseSizeInKB * 1024).toLong()
        return miniAppDatabases.putIfAbsent(
            miniAppId,
            MiniAppDatabase(activity, miniAppId, databaseVersion, maxDBSize)
        ) ?: MiniAppDatabase(activity, miniAppId, databaseVersion, maxDBSize)
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun load(
        miniAppId: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                initOrGetDB(miniAppId)
                onSuccess()
            } catch (e: Exception) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
            }
        }
    }

    fun getDatabaseUsedSize(
        miniAppId: String,
        onSuccess: (Long) -> Unit
    ) {
        val miniAppDatabase = initOrGetDB(miniAppId)
        onSuccess(miniAppDatabase.getDatabaseUsedSize())
    }

    fun insertItems(
        miniAppId: String,
        items: Map<String, String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppDatabase = initOrGetDB(miniAppId)
                if (miniAppDatabase.insert(items)) {
                    onSuccess()
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageInsertItemsFailedError)
                }
            } catch (e: SQLException) {
                if (e.message == MAX_DB_SPACE_LIMIT_REACHED_ERROR) {
                    onFailed(MiniAppSecureStorageError.secureStorageNoSpaceAvailableError)
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
                }
            }
        }
    }

    fun getItem(
        miniAppId: String,
        key: String,
        onSuccess: (String) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppDatabase = initOrGetDB(miniAppId)
                val value = miniAppDatabase.getItem(key)
                onSuccess(value)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
            }
        }
    }

    /**
     * For the future usage, Just in case
     */
    fun getItems(
        miniAppId: String,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppDatabase = initOrGetDB(miniAppId)
                val value = miniAppDatabase.getAllItems()
                if (value.isNotEmpty()) {
                    onSuccess(value)
                }
                else {
                    onSuccess(emptyMap())
                }
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
            }
        }
    }

    /**
     * It will delete given item(s) related to the given mini app id
     */
    fun deleteItems(
        miniAppId: String,
        keySet: Set<String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                val miniAppDatabase = initOrGetDB(miniAppId)
                if (miniAppDatabase.deleteItems(keySet)) {
                    onSuccess()
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageDeleteItemsFailedError)
                }
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
            }
        }
    }

    /**
     * It'll will delete all items/records related to the given mini app id
     */
    fun delete(
        miniAppId: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                clearDatabase(miniAppId)
                onSuccess()
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
            }
        }
    }

    /**
     * It will delete all the records as well as the whole DB related to the given mini app id
     * Will be invoked with MiniApp.clearSecureStorage(miniAppId: String).
     * @param miniAppId will be used to find the file to be deleted.
     */
    fun clearSecureStorage(miniAppId: String) {
        try {
            clearDatabase(miniAppId)
        } catch (e: SQLException) {
            // No callback needed. So Ignoring.
        }
    }

    /**
     * Will be invoked by MiniApp.clearSecureStorage.
     */
    fun clearSecureStorage(databasesCreatedForMiniAppsSet: MutableSet<String>) {
        databasesCreatedForMiniAppsSet.forEach {
            try {
                clearDatabase(it)
            } catch (e: SQLException) {
                // No callback needed. So Ignoring.
            }
        }
    }

    private fun clearDatabase(miniAppId: String) {
        val miniAppDatabase = initOrGetDB(miniAppId)
        miniAppDatabase.deleteAllRecords()
        miniAppDatabase.deleteWholeDB(miniAppId)
        if (miniAppDatabases.isNotEmpty()) {
            miniAppDatabases.clear()
        }
    }
}
