package com.rakuten.tech.mobile.miniapp.storage

import android.app.Activity
import android.database.sqlite.SQLiteException
import androidx.annotation.NonNull
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.database.MAX_DB_SPACE_LIMIT_REACHED_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppSecuredDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.sql.SQLException

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorage(
    @NonNull private val activity: Activity,
    private val databaseVersion: Int,
    private val maxDatabaseSizeInKB: Int
) {

    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var miniAppSecuredDatabase: MiniAppSecuredDatabase

    private fun checkAndInitSecuredDatabase(miniAppId: String) {
        if (!this::miniAppSecuredDatabase.isInitialized) {
            val maxDBSize = (maxDatabaseSizeInKB * 1024).toLong()
            miniAppSecuredDatabase =
                MiniAppSecuredDatabase(activity, miniAppId, databaseVersion, maxDBSize)
        }
    }

    private fun createOrOpenAndUnlockDatabase() {
        miniAppSecuredDatabase.createAndOpenDatabase()
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun load(
        miniAppId: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                checkAndInitSecuredDatabase(miniAppId)
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
        onSuccess(miniAppSecuredDatabase.getDatabaseUsedSize())
    }

    fun insertItems(
        miniAppId: String,
        items: Map<String, String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (!miniAppSecuredDatabase.isDatabaseAvailable(miniAppId)) {
                    createOrOpenAndUnlockDatabase()
                }

                if (miniAppSecuredDatabase.insert(items)) {
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
            } catch (e: SQLiteException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
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
                val value = miniAppSecuredDatabase.getItem(key)
                if (value != null) {
                    onSuccess(value)
                }
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
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
                val value = miniAppSecuredDatabase.getAllItems()
                if (value != null) {
                    if (value.isNotEmpty()) {
                        onSuccess(value)
                    } else {
                        onSuccess(emptyMap())
                    }
                }
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
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
                if (miniAppSecuredDatabase.deleteItems(keySet)) {
                    onSuccess()
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageDeleteItemsFailedError)
                }
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
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
            } catch (e: IOException) {
                onFailed(MiniAppSecureStorageError.secureStorageFatalDatabaseRuntimeError)
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
        } catch (e: IOException) {
            // No callback needed. So Ignoring.
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
            } catch (e: IOException) {
                // No callback needed. So Ignoring.
            } catch (e: SQLException) {
                // No callback needed. So Ignoring.
            }
        }
    }

    private fun clearDatabase(miniAppId: String) {
        miniAppSecuredDatabase.deleteAllRecords()
        miniAppSecuredDatabase.deleteWholeDB(miniAppId)
    }
}
