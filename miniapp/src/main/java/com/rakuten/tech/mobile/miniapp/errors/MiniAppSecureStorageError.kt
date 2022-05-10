package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep

/**
 * A class to provide the custom errors specific for secure storage.
 */
@Keep
internal class MiniAppSecureStorageError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        private const val SecureStorageFullError = "SecureStorageFullError"
        private const val SecureStorageIOError = "SecureStorageIOError"
        private const val SecureStorageUnavailableError = "SecureStorageUnavailableError"
        private const val SecureStorageBusyError = "SecureStorageBusyError"

        // Secure Storage is full.
        val secureStorageFullError =
            MiniAppSecureStorageError(
                SecureStorageFullError,
                errorDescription(SecureStorageFullError)
            )

        // Failed to read/write secure storage.
        val secureStorageIOError =
            MiniAppSecureStorageError(SecureStorageIOError, errorDescription(SecureStorageIOError))

        // Secure storage unavailable.
        val secureStorageUnavailableError =
            MiniAppSecureStorageError(
                SecureStorageUnavailableError,
                errorDescription(SecureStorageUnavailableError)
            )

        // Error when storage is busy.
        val secureStorageBusyError =
            MiniAppSecureStorageError(
                SecureStorageBusyError,
                errorDescription(SecureStorageBusyError)
            )

        private fun errorDescription(error: String): String {
            return when (error) {
                SecureStorageFullError -> "Secure storage is full."
                SecureStorageIOError -> "Failed to read/write secure storage."
                SecureStorageUnavailableError -> "Secure storage unavailable."
                SecureStorageBusyError -> "Storage is busy."
                else -> ""
            }
        }
    }
}
