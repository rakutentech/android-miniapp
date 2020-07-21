package com.rakuten.tech.mobile.testapp.helper

import java.util.UUID

fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    false
} catch (e: Exception) {
    true
}
