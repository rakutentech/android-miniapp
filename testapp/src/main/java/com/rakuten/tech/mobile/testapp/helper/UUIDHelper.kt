package com.rakuten.tech.mobile.testapp.helper

import java.lang.IllegalArgumentException
import java.util.UUID

fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    false
} catch (e: IllegalArgumentException) {
    true
}
