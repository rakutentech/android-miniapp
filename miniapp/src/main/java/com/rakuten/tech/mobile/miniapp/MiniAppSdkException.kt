package com.rakuten.tech.mobile.miniapp

import android.os.NetworkOnMainThreadException

/**
 * A custom exception class which treats the purpose of providing
 * error information to the consumer app in an unified way.
 */
open class MiniAppSdkException(message: String, cause: Throwable?) : Exception(message, cause) {

    constructor(e: Exception) : this(exceptionMessage(e), e)

    constructor(message: String) : this(message, null)
}

internal class MiniAppNetException(message: String, cause: Throwable?) : MiniAppSdkException(message, cause) {

    constructor(e: Exception) : this("Found some problem, ${e.message}", e)

    constructor(message: String) : this(message, null)
}

@Suppress("FunctionMaxLength")
internal fun sdkExceptionForInternalServerError() = MiniAppSdkException("Internal server error")

@Suppress("FunctionMaxLength")
internal fun sdkExceptionForInvalidArguments(message: String = "") =
    MiniAppSdkException(
        "Invalid arguments${when {
            message.isNotBlank() -> ": $message"
            else -> ""
        }}"
    )

@Suppress("FunctionMaxLength")
internal fun sdkExceptionForNoActivityContext() =
    MiniAppSdkException("Only accept context of type Activity or ActivityCompat")

private fun exceptionMessage(exception: Exception) = when (exception) {
    is NetworkOnMainThreadException -> "Network requests must not be performed on the main thread. "
        .plus("Use Dispatchers.IO or Dispatchers.Default for MiniApp suspending functions.")
    else -> "Found some problem, ${exception.message}"
}
