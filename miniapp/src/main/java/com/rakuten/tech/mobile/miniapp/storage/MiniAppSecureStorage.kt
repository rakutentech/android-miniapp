package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.js.DB_NAME_PREFIX
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppSecureDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteFullException
import java.io.IOException
import java.sql.SQLException

@Suppress("LargeClass", "TooManyFunctions")
internal class MiniAppSecureStorage(
    @NonNull private val context: Context,
    private val databaseVersion: Int,
    private val maxDatabaseSizeInBytes: Long
) {

    @VisibleForTesting
    internal lateinit var databaseName: String

    @VisibleForTesting
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @VisibleForTesting
    internal lateinit var miniAppSecureDatabase: MiniAppSecureDatabase

    @Suppress("MagicNumber")
    private fun checkAndInitSecuredDatabase(miniAppId: String) {
        if (!this::miniAppSecureDatabase.isInitialized) {
            setDatabaseName(miniAppId)
            miniAppSecureDatabase =
                MiniAppSecureDatabase(context, databaseName, databaseVersion, maxDatabaseSizeInBytes)
        }
    }

    private fun setDatabaseName(miniAppId: String) {
        databaseName = DB_NAME_PREFIX + miniAppId
    }

    @SuppressWarnings("ExpressionBodySyntax", "RethrowCaughtException", "TooGenericExceptionCaught")
    private fun createOrOpenAndUnlockDatabase(): Boolean {
        var status: Boolean
        try {
            status = miniAppSecureDatabase.createAndOpenDatabase()
        } catch (e: RuntimeException) {
            throw e
        }
        return status
    }

    private fun validatePreCheck(onFailed: (MiniAppSecureStorageError) -> Unit): Boolean {
        val status: Boolean = if (!miniAppSecureDatabase.isDatabaseAvailable(databaseName)) {
            onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
            false
        } else if (miniAppSecureDatabase.isDatabaseBusy()) {
            onFailed(MiniAppSecureStorageError.secureStorageBusyError)
            false
        } else {
            true
        }
        return status
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
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    fun getDatabaseUsedSize(
        onSuccess: (Long) -> Unit
    ) {
        onSuccess(miniAppSecureDatabase.getDatabaseUsedSize())
    }

    internal fun closeDatabase() {
        if (this::miniAppSecureDatabase.isInitialized)
            miniAppSecureDatabase.closeDatabase()
    }

    @Suppress("ComplexMethod", "SwallowedException", "TooGenericExceptionCaught")
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

                if (miniAppSecureDatabase.isDatabaseBusy()) {
                    onFailed(MiniAppSecureStorageError.secureStorageBusyError)
                } else if (miniAppSecureDatabase.isDatabaseFull()) {
                    onFailed(MiniAppSecureStorageError.secureStorageFullError)
                } else {
                    if (miniAppSecureDatabase.insert(items)) {
                        onSuccess()
                    } else {
                        onFailed(MiniAppSecureStorageError.secureStorageIOError)
                    }
                }
            } catch (e: IllegalStateException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLiteFullException) {
                onFailed(MiniAppSecureStorageError.secureStorageFullError)
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun getItem(
        key: String,
        onSuccess: (String) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (validatePreCheck(onFailed)) {
                    val value = miniAppSecureDatabase.getItem(key)
                    onSuccess(value)
                }
            } catch (e: IllegalStateException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * Kept For the future reference, Just in case.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun getAllItems(
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (validatePreCheck(onFailed)) {
                    val value = miniAppSecureDatabase.getAllItems()
                    if (value.isNotEmpty()) {
                        onSuccess(value)
                    } else {
                        onSuccess(emptyMap())
                    }
                }
            } catch (e: IllegalStateException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * It will delete given item(s) related to the given mini app id.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun deleteItems(
        keySet: Set<String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (validatePreCheck(onFailed)) {
                    if (miniAppSecureDatabase.deleteItems(keySet)) {
                        onSuccess()
                    } else {
                        onFailed(MiniAppSecureStorageError.secureStorageIOError)
                    }
                }
            } catch (e: IllegalStateException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    /**
     * It'll will delete all items/records related to the given mini app id.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun delete(
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            try {
                if (validatePreCheck(onFailed)) {
                    clearDatabase(databaseName)
                    onSuccess()
                }
            } catch (e: IOException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: IllegalStateException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: SQLException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            } catch (e: RuntimeException) {
                onFailed(MiniAppSecureStorageError.secureStorageIOError)
            }
        }
    }

    @Suppress("RethrowCaughtException", "TooGenericExceptionCaught")
    private fun clearDatabase(dbName: String) {
        try {
            miniAppSecureDatabase.deleteAllRecords()
            miniAppSecureDatabase.deleteWholeDatabase(dbName)
        } catch (e: RuntimeException) {
            throw e
        }
    }
}
