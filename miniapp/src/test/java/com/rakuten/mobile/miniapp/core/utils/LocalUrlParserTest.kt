package com.rakuten.mobile.miniapp.core.utils

import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.core.BaseTest
import org.junit.Before
import org.junit.Test

class LocalUrlParserTest : BaseTest() {

    lateinit var urlParser: LocalUrlParser
    lateinit var invalideUrlParser: LocalUrlParser

    @Before
    fun setUp() {
        urlParser = LocalUrlParser()
        invalideUrlParser = LocalUrlParser()
    }

    @Test
    fun shouldGetAppIdFromValidUrl() {
        assertThat(urlParser.getAppId(VALID_MANIFEST_ENDPOINT))
            .isEqualTo("78d85043-d04f-486a-8212-bf2601cb63a2")
    }

    @Test
    fun shouldGetEmptyAppIdFromInvalidUrl() {
        assertThat(invalideUrlParser.getAppId(INVALID_MANIFEST_ENDPOINT)).isEqualTo("")
    }

    @Test
    fun shouldGetVersionIdFromUrl() {
        assertThat(urlParser.getVersionId(VALID_MANIFEST_ENDPOINT))
            .isEqualTo("17bccee1-17f0-44fa-8cb8-2da89eb49905")
    }

    @Test
    fun shouldGetEmptyVersionIdFromInvalidUrl() {
        assertThat(invalideUrlParser.getVersionId(INVALID_MANIFEST_ENDPOINT)).isEqualTo("")
    }

    @Test
    fun shouldGetFilePathWithValidUrl() {
        assertThat(urlParser.getFilePath(VALID_FILE_URL_PATH)).isEqualTo("/js/")
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
