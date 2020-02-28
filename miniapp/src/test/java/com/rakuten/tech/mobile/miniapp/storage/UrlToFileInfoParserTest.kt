package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.INVALID_FILE_URL_PATH
import com.rakuten.tech.mobile.miniapp.VALID_FILE_URL_PATH
import org.amshove.kluent.shouldEqual
import org.junit.Test

class UrlToFileInfoParserTest {

    private var urlParser = UrlToFileInfoParser()

    @Test
    fun shouldGetFilePathWithValidUrl() {
        urlParser.getFilePath(VALID_FILE_URL_PATH) shouldEqual "/a/b/"
    }

    @Test
    fun shouldGetEmptyFilePathWithInvalidUrl() {
        urlParser.getFilePath(INVALID_FILE_URL_PATH) shouldEqual ""
    }

    @Test
    fun shouldGetFileNameWithValidUrl() {
        urlParser.getFileName(VALID_FILE_URL_PATH) shouldEqual "index.html"
    }

    @Test
    fun shouldGetFileNameWithInvalidUrl() {
        urlParser.getFileName(INVALID_FILE_URL_PATH) shouldEqual ""
    }
}
