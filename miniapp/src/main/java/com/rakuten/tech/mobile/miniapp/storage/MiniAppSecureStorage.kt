package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.js.DB_NAME_PREFIX
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_BUSY_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_UNAVAILABLE_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_SPACE_LIMIT_REACHED_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppSecureDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.sql.SQLException

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorage(
    @NonNull private val context: Context,
    private val databaseVersion: Int,
    private val maxDatabaseSizeInKB: Int
) {

    @VisibleForTesting
    internal lateinit var databaseName: String

    @VisibleForTesting
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @VisibleForTesting
    internal lateinit var miniAppSecureDatabase: MiniAppSecureDatabase

    private fun checkAndInitSecuredDatabase(miniAppId: String) {
        if (!this::miniAppSecureDatabase.isInitialized) {
            val maxDBSize = (maxDatabaseSizeInKB * 1024).toLong()
            setDatabaseName(miniAppId)
            miniAppSecureDatabase =
                MiniAppSecureDatabase(context, databaseName, databaseVersion, maxDBSize)
        }
    }

    private fun setDatabaseName(miniAppId: String) {
        databaseName = DB_NAME_PREFIX + miniAppId
    }

    private fun createOrOpenAndUnlockDatabase(): Boolean {
        return miniAppSecureDatabase.createAndOpenDatabase()
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
                if (createOrOpenAndUnlockDatabase()) {
                    onSuccess()
                }
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    fun getDatabaseUsedSize(
        onSuccess: (Long) -> Unit
    ) {
        onSuccess(miniAppSecureDatabase.getDatabaseUsedSize())
    }

    fun insertItems(
        items: Map<String, String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (!miniAppSecureDatabase.isDatabaseAvailable(databaseName)) {
                    createOrOpenAndUnlockDatabase()
                }

                if (miniAppSecureDatabase.insert(items)) {
                    onSuccess()
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } catch (e: SQLiteException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                when(e.message) {
                    DATABASE_BUSY_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageBusyError)
                    DATABASE_SPACE_LIMIT_REACHED_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageFullError)
                    else -> onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            }
        }
    }

    fun getItem(
        key: String,
        onSuccess: (String) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {

        scope.launch {
            try {
                val value = miniAppSecureDatabase.getItem(key)
                onSuccess(value)
            } catch (e: SQLException) {
                when(e.message) {
                    DATABASE_BUSY_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageBusyError)
                    DATABASE_UNAVAILABLE_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
                    else -> onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * Kept For the future reference, Just in case.
     */
    fun getAllItems(
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                val value = miniAppSecureDatabase.getAllItems()
                if (value.isNotEmpty()) {
                    onSuccess(value)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: SQLException) {
                when(e.message) {
                    DATABASE_BUSY_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageBusyError)
                    DATABASE_UNAVAILABLE_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
                    else -> onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * It will delete given item(s) related to the given mini app id
     */
    fun deleteItems(
        keySet: Set<String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (miniAppSecureDatabase.deleteItems(keySet)) {
                    onSuccess()
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } catch (e: SQLException) {
                when(e.message) {
                    DATABASE_BUSY_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageBusyError)
                    DATABASE_UNAVAILABLE_ERROR -> onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
                    else -> onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * It'll will delete all items/records related to the given mini app id
     */
    fun delete(
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                clearDatabase(databaseName)
                onSuccess()
            } catch (e: IOException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                if (e.message.equals(DATABASE_UNAVAILABLE_ERROR)) {
                    onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
                }
                else {
                    onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            }
        }
    }

    /**
     * In case if host app want to clear the database for a specific MiniApp
     * then It will delete all the records as well as the whole DB
     * related to the given mini app id.
     *
     * @param miniAppId will be used to find the file to be deleted.
     */
    fun clearSecureDatabase(miniAppId: String) {
        try {
            val dbName = DB_NAME_PREFIX + miniAppId
            context.deleteDatabase(dbName)
        } catch (e: Exception) {
            // No callback needed. So Ignoring.
        }
    }

    /**
     * In case if host app want to clear all the database for every MiniApp
     * then It will delete all the records as well as the whole DB
     * for all the MiniApps who created a Database.
     */
    fun clearAllSecureDatabases() {
        try {
            context.databaseList().forEach {
                if (it.startsWith(DB_NAME_PREFIX)) {
                    context.deleteDatabase(it)
                }
            }
        } catch (e: Exception) {
            // No callback needed. So Ignoring.
        }
    }

    private fun clearDatabase(dbName: String) {
        miniAppSecureDatabase.deleteAllRecords()
        miniAppSecureDatabase.deleteWholeDatabase(dbName)
    }
}
