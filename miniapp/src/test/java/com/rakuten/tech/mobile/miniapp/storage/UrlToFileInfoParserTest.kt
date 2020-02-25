package com.rakuten.tech.mobile.miniapp.storage

import com.google.common.truth.Truth.assertThat
import com.rakuten.tech.mobile.miniapp.INVALID_FILE_URL_PATH
import com.rakuten.tech.mobile.miniapp.VALID_FILE_URL_PATH
import org.junit.Before
import org.junit.Test

class UrlToFileInfoParserTest {

    lateinit var urlParser: UrlToFileInfoParser

    @Before
    fun setUp() {
        urlParser = UrlToFileInfoParser()
    }

    @Test
    fun shouldGetFilePathWithValidUrl() {
        assertThat(urlParser.getFilePath(VALID_FILE_URL_PATH)).isEqualTo("/a/b/")
    }

    @Test
    fun shouldGetEmptyFilePathWithInvalidUrl() {
        assertThat(urlParser.getFilePath(INVALID_FILE_URL_PATH)).isEqualTo("")
    }

    @Test
    fun shouldGetFileNameWithValidUrl() {
        assertThat(urlParser.getFileName(VALID_FILE_URL_PATH)).isEqualTo("index.html")
    }

    @Test
    fun shouldGetFileNameWithInvalidUrl() {
        assertThat(urlParser.getFileName(INVALID_FILE_URL_PATH)).isEqualTo("")
    }
}
