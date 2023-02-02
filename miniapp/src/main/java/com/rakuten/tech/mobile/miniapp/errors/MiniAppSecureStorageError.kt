package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_BUSY_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_IO_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_UNAVAILABLE_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_SPACE_LIMIT_REACHED_ERROR

/**
 * A class to provide the custom errors specific for secure storage.
 */
@Keep
internal class MiniAppSecureStorageError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        @VisibleForTesting internal const val SecureStorageIOError = "SecureStorageIOError"
        @VisibleForTesting internal const val SecureStorageFullError = "SecureStorageFullError"
        @VisibleForTesting internal const val SecureStorageBusyError = "SecureStorageBusyError"
        @VisibleForTesting internal const val SecureStorageUnavailableError = "SecureStorageUnavailableError"

        // Failed to read/write secure storage.
        val secureStorageIOError =
            MiniAppSecureStorageError(
                SecureStorageIOError,
                errorDescription(SecureStorageIOError)
            )

        val secureStorageFullError =
            MiniAppSecureStorageError(
                SecureStorageFullError,
                errorDescription(SecureStorageFullError)
            )

        val secureStorageBusyError =
            MiniAppSecureStorageError(
                SecureStorageBusyError,
                errorDescription(SecureStorageBusyError)
            )

        val secureStorageUnavailableError =
            MiniAppSecureStorageError(
                SecureStorageUnavailableError,
                errorDescription(SecureStorageUnavailableError)
            )

        @VisibleForTesting
        internal fun errorDescription(error: String): String {
            return when (error) {
                SecureStorageIOError -> DATABASE_IO_ERROR
                SecureStorageBusyError -> DATABASE_BUSY_ERROR
                SecureStorageFullError -> DATABASE_SPACE_LIMIT_REACHED_ERROR
                SecureStorageUnavailableError -> DATABASE_UNAVAILABLE_ERROR
                else -> ""
            }
        }
    }
}
