package com.rakuten.tech.mobile.miniapp.storage

import com.google.common.truth.Truth.assertThat
import com.rakuten.tech.mobile.miniapp.INVALID_FILE_URL_PATH
import com.rakuten.tech.mobile.miniapp.INVALID_MANIFEST_ENDPOINT
import com.rakuten.tech.mobile.miniapp.VALID_FILE_URL_PATH
import com.rakuten.tech.mobile.miniapp.VALID_MANIFEST_ENDPOINT
import org.junit.Before
import org.junit.Test

class UrlToFileInfoParserTest {

    lateinit var urlParser: UrlToFileInfoParser
    lateinit var invalideUrlParser: UrlToFileInfoParser

    @Before
    fun setUp() {
        urlParser = UrlToFileInfoParser()
        invalideUrlParser = UrlToFileInfoParser()
    }

    @Test
    fun shouldGetAppIdFromValidUrl() {
        assertThat(urlParser.getAppIdForLegacy(VALID_MANIFEST_ENDPOINT))
            .isEqualTo("78d85043-d04f-486a-8212-bf2601cb63a2")
    }

    @Test
    fun shouldGetEmptyAppIdFromInvalidUrl() {
        assertThat(invalideUrlParser.getAppIdForLegacy(INVALID_MANIFEST_ENDPOINT)).isEqualTo("")
    }

    @Test
    fun shouldGetVersionIdFromUrl() {
        assertThat(urlParser.getVersionIdForLegacy(VALID_MANIFEST_ENDPOINT))
            .isEqualTo("17bccee1-17f0-44fa-8cb8-2da89eb49905")
    }

    @Test
    fun shouldGetEmptyVersionIdFromInvalidUrl() {
        assertThat(invalideUrlParser.getVersionIdForLegacy(INVALID_MANIFEST_ENDPOINT)).isEqualTo("")
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
