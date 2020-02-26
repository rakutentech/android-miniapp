package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.INVALID_FILE_URL_PATH
import com.rakuten.tech.mobile.miniapp.VALID_FILE_URL_PATH
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class UrlToFileInfoParserTest {

    private lateinit var urlParser: UrlToFileInfoParser

    @Before
    fun setUp() {
        urlParser = UrlToFileInfoParser()
    }

    @Test
    fun shouldGetFilePathWithValidUrl() {
        assertTrue { urlParser.getFilePath(VALID_FILE_URL_PATH) == "/a/b/" }
    }

    @Test
    fun shouldGetEmptyFilePathWithInvalidUrl() {
        assertTrue { urlParser.getFilePath(INVALID_FILE_URL_PATH) == "" }
    }

    @Test
    fun shouldGetFileNameWithValidUrl() {
        assertTrue { urlParser.getFileName(VALID_FILE_URL_PATH) == "index.html" }
    }

    @Test
    fun shouldGetFileNameWithInvalidUrl() {
        assertTrue { urlParser.getFileName(INVALID_FILE_URL_PATH) == "" }
    }
}
