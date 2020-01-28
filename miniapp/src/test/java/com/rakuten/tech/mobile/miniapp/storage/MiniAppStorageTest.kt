package com.rakuten.tech.mobile.miniapp.storage

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import com.rakuten.tech.mobile.miniapp.legacy.core.utils.LocalUrlParser
import org.junit.Test

class MiniAppStorageTest {

    @Test
    fun `for a given set of app & version id formed base path is retured`() {
        val miniAppStorage = getMockedMiniAppStorage()
        assertThat(
            miniAppStorage.getSavePathForApp(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        )
            .isEqualTo("null/miniapp/$TEST_ID_MINIAPP/$TEST_ID_MINIAPP_VERSION/")
    }

    @Test
    fun `for a given set base & file path with file's name, formed absolute path is retured`() {
        val miniAppStorage = getMockedMiniAppStorage()
        assertThat(miniAppStorage.getAbsoluteWritePath("a", "b", "c"))
            .isEqualTo("abc")
    }

    @Test
    fun `for a given url file path is retured via LocalUrlParser`() {
        val localUrlParser = getMockedLocalUrlParser()
        val miniAppStorage = MiniAppStorage(mock(), mock(), localUrlParser)
        miniAppStorage.getFilePath(TEST_URL_FILE)
        verify(localUrlParser, times(1)).getFilePath(TEST_URL_FILE)
    }

    @Test
    fun `for a given url file name is retured via LocalUrlParser`() {
        val localUrlParser = getMockedLocalUrlParser()
        val miniAppStorage = MiniAppStorage(mock(), mock(), localUrlParser)
        miniAppStorage.getFileName(TEST_URL_FILE)
        verify(localUrlParser, times(1)).getFileName(TEST_URL_FILE)
    }

    private fun getMockedMiniAppStorage() = MiniAppStorage(mock(), mock(), mock())

    private fun getMockedLocalUrlParser() = mock<LocalUrlParser>()
}
