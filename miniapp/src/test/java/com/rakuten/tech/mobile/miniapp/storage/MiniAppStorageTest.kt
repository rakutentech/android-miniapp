package com.rakuten.tech.mobile.miniapp.storage

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import org.junit.Test
import kotlin.test.assertTrue

class MiniAppStorageTest {

    @Test
    fun `for a given set of app & version id formed base path is returned`() {
        val miniAppStorage = getMockedMiniAppStorage()
        assertTrue {
            miniAppStorage.getSavePathForApp(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) == "null/miniapp/$TEST_ID_MINIAPP/$TEST_ID_MINIAPP_VERSION"
        }
    }

    @Test
    fun `for a given set of base path & file path, formed parent path is returned`() {
        val miniAppStorage = getMockedMiniAppStorage()
        assertTrue { miniAppStorage.getAbsoluteWritePath("a", "b", "c") == "abc" }
    }

    @Test
    fun `for a given url file path is returned via LocalUrlParser`() {
        val localUrlParser = getMockedLocalUrlParser()
        val miniAppStorage = MiniAppStorage(mock(), mock(), localUrlParser)
        miniAppStorage.getFilePath(TEST_URL_FILE)
        verify(localUrlParser, times(1)).getFilePath(TEST_URL_FILE)
    }

    @Test
    fun `for a given url file name is returned via LocalUrlParser`() {
        val localUrlParser = getMockedLocalUrlParser()
        val miniAppStorage = MiniAppStorage(mock(), mock(), localUrlParser)
        miniAppStorage.getFileName(TEST_URL_FILE)
        verify(localUrlParser, times(1)).getFileName(TEST_URL_FILE)
    }

    private fun getMockedMiniAppStorage() = MiniAppStorage(mock(), mock(), mock())

    private fun getMockedLocalUrlParser() = mock<UrlToFileInfoParser>()
}
