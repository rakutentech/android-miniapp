package com.rakuten.tech.mobile.miniapp

/**
 * A custom exception class which treats the purpose of providing
 * error information to the consumer app in an unified way.
 */
class MiniAppSdkException(message: String) : Exception(message) {

    constructor(e: Exception) : this("Found some problem, ${e.message}")
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

internal fun sdkExceptionForInvalidVersion() =
    MiniAppSdkException("Invalid or unpublished MiniApp version")
