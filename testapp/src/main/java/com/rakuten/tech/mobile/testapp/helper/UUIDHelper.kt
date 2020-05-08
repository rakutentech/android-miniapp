package com.rakuten.tech.mobile.testapp.helper

import java.lang.Exception
import java.util.*

fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    false
} catch (e: Exception) {
    true
}
