package com.rakuten.tech.mobile.miniapp.storage

import java.io.File

internal class UrlToFileInfoParser {

    /**
     * Returns the last element of the split string. Returns empty String when not found.
     */
    fun getFileName(fileUrl: String): String = fileUrl.split(File.separator).run {
        return when {
            "." in last() -> last()
            else -> ""
        }
    }
}
