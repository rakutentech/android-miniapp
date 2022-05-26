package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.storage.database.MAX_DB_SPACE_LIMIT_REACHED_ERROR

/**
 * A class to provide the custom errors specific for secure storage.
 */
@Keep
internal class MiniAppSecureStorageError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        private const val SecureStorageNoSpaceAvailableError = "SecureStorageNoSpaceAvailableError"
        private const val SecureStorageInsertItemsFailedError = "SecureStorageInsertItemsFailedError"
        private const val SecureStorageDeleteItemsFailedError = "SecureStorageDeleteItemsFailedError"
        private const val SecureStorageFatalDatabaseRuntimeError = "SecureStorageFatalDatabaseRuntimeError"

        // Failed to read/write secure storage.
        val secureStorageFatalDatabaseRuntimeError =
            MiniAppSecureStorageError(
                SecureStorageFatalDatabaseRuntimeError,
                errorDescription(SecureStorageFatalDatabaseRuntimeError)
            )

        val secureStorageNoSpaceAvailableError =
            MiniAppSecureStorageError(
                SecureStorageNoSpaceAvailableError,
                errorDescription(SecureStorageNoSpaceAvailableError)
            )

        val secureStorageInsertItemsFailedError =
            MiniAppSecureStorageError(
                SecureStorageInsertItemsFailedError,
                errorDescription(SecureStorageInsertItemsFailedError)
            )

        val secureStorageDeleteItemsFailedError =
            MiniAppSecureStorageError(
                SecureStorageDeleteItemsFailedError,
                errorDescription(SecureStorageDeleteItemsFailedError)
            )

        private fun errorDescription(error: String): String {
            return when (error) {
                SecureStorageNoSpaceAvailableError -> MAX_DB_SPACE_LIMIT_REACHED_ERROR
                SecureStorageInsertItemsFailedError -> "Error occurred. Failed to insert items."
                SecureStorageDeleteItemsFailedError -> "Failed to delete Items. Could not find the items."
                SecureStorageFatalDatabaseRuntimeError -> "Fatal runtime error occurred during I/O operation with database."
                else -> ""
            }
        }
    }
}
