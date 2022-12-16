package com.rakuten.tech.mobile.miniapp.errors

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting

/**
 * A class to provide the custom errors specific for file download.
 */
@Keep
class MiniAppDownloadFileError(val type: String? = null, val message: String? = null, val code: Int? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        @VisibleForTesting internal const val DownloadFailedError = "DownloadFailedError"
        @VisibleForTesting internal const val InvalidUrlError = "InvalidUrlError"
        @VisibleForTesting internal const val SaveFailureError = "SaveFailureError"
        private const val DownloadHttpError = "DownloadHttpError"

        // Failed to download file.
        val downloadFailedError = MiniAppDownloadFileError(DownloadFailedError, errorDescription(DownloadFailedError))

        // Requested Url is invalid.
        val invalidUrlError = MiniAppDownloadFileError(InvalidUrlError, errorDescription(InvalidUrlError))

        val saveFailureError = MiniAppDownloadFileError(SaveFailureError, errorDescription(SaveFailureError))

        internal fun httpError(code: Int, message: String) = MiniAppDownloadFileError(
            type = DownloadHttpError,
            code = code,
            message = message
        )

        @VisibleForTesting
        internal fun errorDescription(error: String): String {
            return when (error) {
                DownloadFailedError -> "Failed to download the file."
                InvalidUrlError -> "URL is invalid."
                SaveFailureError -> "Save file temporarily failed"
                else -> ""
            }
        }
    }
}
