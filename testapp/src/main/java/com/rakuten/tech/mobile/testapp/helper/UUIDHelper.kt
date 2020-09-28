package com.rakuten.tech.mobile.testapp.helper

import java.lang.IllegalArgumentException
import java.util.UUID

private const val UUID_LENGTH = 36
fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    this.length != UUID_LENGTH
} catch (e: IllegalArgumentException) {
    true
}
