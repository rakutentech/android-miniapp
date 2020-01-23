package com.rakuten.tech.mobile.miniapp

/**
 * A custom exception class which treats the purpose of providing
 * error information to the consumer app in an unified way.
 */
class MiniAppSdkException(message: String) : Exception(message) {

    constructor(e: Exception) : this("Found some problem, $e.message")
}
