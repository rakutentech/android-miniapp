package com.rakuten.tech.mobile.miniapp.errors

/**
 * A class to provide the custom errors specific for file download.
 */
class MiniAppDownloadFileError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        private const val DownloadFailedError = "DownloadFailedError"
        private const val InvalidUrlError = "InvalidUrlError"
        private const val SaveFailureError = "SaveFailureError"

        // Failed to download file.
        val downloadFailedError = MiniAppDownloadFileError(DownloadFailedError, errorDescription(DownloadFailedError))

        // Requested Url is invalid.
        val invalidUrlError = MiniAppDownloadFileError(InvalidUrlError, errorDescription(InvalidUrlError))

        val saveFailureError = MiniAppDownloadFileError(SaveFailureError, errorDescription(SaveFailureError))

        /**
         *  send custom error message from host app.
         *  @property code error code send to miniapp.
         *  @property reason error message send to mini app.
         */
        fun custom(code: String, reason: String) = MiniAppDownloadFileError(code, reason)

        private fun errorDescription(error: String): String {
            return when (error) {
                DownloadFailedError -> "Failed to download the file."
                InvalidUrlError -> "URL is invalid."
                SaveFailureError -> "Save file temporarily failed"
                else -> ""
            }
        }
    }
}
