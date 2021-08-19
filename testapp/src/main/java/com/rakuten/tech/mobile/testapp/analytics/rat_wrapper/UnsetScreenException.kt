package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

/**
 * Exception to be thrown when activities that do not set the screen name
 * for RAT screen view events
 */
class UnsetScreenException(message: String?) : Exception(message)
