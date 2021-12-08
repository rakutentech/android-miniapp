package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import java.util.regex.Pattern

internal fun String.isValidLocale(): Boolean =
    Pattern.compile("^[a-z]{2}(-[A-Z]{2})?\$").matcher(this).matches()
