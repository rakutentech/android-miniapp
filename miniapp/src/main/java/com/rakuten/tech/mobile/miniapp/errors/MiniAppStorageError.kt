package com.rakuten.tech.mobile.miniapp.errors

/**
 * A class to provide the custom errors specific for secure storage.
 */
class MiniAppStorageError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        private const val SecureStorageFullError = "SecureStorageFullError"
        private const val IOError = "IOError"
        private const val UnavailableStorage = "UnavailableStorage"
        private const val UnavailableItem = "UnavailableItem"
        private const val EmptyStorage = "EmptyStorage"
        private const val FailedDeleteError = "FailedDeleteError"
        private const val StorageOccupiedError = "StorageOccupiedError"

        // Secure Storage is full.
        val secureStorageFullError = MiniAppStorageError(SecureStorageFullError, errorDescription(SecureStorageFullError))

        // Failed to read/write secure storage.
        val ioError = MiniAppStorageError(IOError, errorDescription(IOError))

        // Secure storage unavailable.
        val unavailableStorage = MiniAppStorageError(UnavailableStorage, errorDescription(UnavailableStorage))

        // Item unavailable.
        val unavailableItem = MiniAppStorageError(UnavailableItem, errorDescription(UnavailableItem))

        // Storage has no item.
        val emptyStorage = MiniAppStorageError(EmptyStorage, errorDescription(EmptyStorage))

        // Failed to delete storage.
        val failedDeleteError = MiniAppStorageError(FailedDeleteError, errorDescription(FailedDeleteError))

        // Failed to delete storage.
        val storageOccupiedError = MiniAppStorageError(StorageOccupiedError, errorDescription(StorageOccupiedError))

        /**
         *  send custom error message from host app.
         *  @property reason error message send to mini app.
         */
        fun custom(reason: String) = MiniAppStorageError(reason)

        private fun errorDescription(error: String): String {
            return when (error) {
                SecureStorageFullError -> "Secure storage is full."
                IOError -> "Failed to read/write secure storage."
                UnavailableStorage -> "Secure storage unavailable."
                UnavailableItem -> "Item unavailable."
                EmptyStorage -> "No item available in storage."
                FailedDeleteError -> "Failed to delete storage."
                StorageOccupiedError -> "Storage is occupied."
                else -> ""
            }
        }
    }
}

