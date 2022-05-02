package com.rakuten.tech.mobile.miniapp.errors

/**
 * A class to provide the custom errors specific for secure storage.
 */
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

        // Failed to delete storage.
        val secureStorageBusyError =
            MiniAppSecureStorageError(
                SecureStorageBusyError,
                errorDescription(SecureStorageBusyError)
            )

        /**
         *  send custom error message from host app.
         *  @property reason error message send to mini app.
         */
        fun custom(reason: String) = MiniAppSecureStorageError(reason)

        private fun errorDescription(error: String): String {
            return when (error) {
                SecureStorageFullError -> "Secure storage is full."
                SecureStorageIOError -> "Failed to read/write secure storage."
                SecureStorageUnavailableError -> "Secure storage unavailable."
                SecureStorageBusyError -> "Storage is occupied."
                else -> ""
            }
        }
    }
}
