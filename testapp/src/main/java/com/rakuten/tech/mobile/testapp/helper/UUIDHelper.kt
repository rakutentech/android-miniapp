package com.rakuten.tech.mobile.testapp.helper

import java.lang.IllegalArgumentException
import java.util.UUID

fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    false
} catch (e: IllegalArgumentException) {
    true
}

fun String.includeHyphen(): String =
    replace("(.{8})(.{4})(.{4})(.{4})(.+)".toRegex(), "$1-$2-$3-$4-$5")
