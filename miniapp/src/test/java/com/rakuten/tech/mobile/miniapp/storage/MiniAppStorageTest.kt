package com.rakuten.tech.mobile.miniapp.storage

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.api.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.legacy.core.utils.LocalUrlParser
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class MiniAppStorageTest {

    private val TEST_URL = "https://www.example.com/1"

    @Test
    fun `for a given set of app & version id formed base path is retured`() {
        val miniAppStorage = getMockedMiniAppStorage()
        assertThat(miniAppStorage.getSavePathForApp(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION))
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
        val miniAppStorage = MiniAppStorage (mock(), mock(), localUrlParser)
        miniAppStorage.getFilePathFromUrl(TEST_URL)
        verify(localUrlParser, times(1)).getFilePath(TEST_URL)
    }

    @Test
    fun `for a given url file name is retured via LocalUrlParser`() {
        val localUrlParser = getMockedLocalUrlParser()
        val miniAppStorage = MiniAppStorage (mock(), mock(), localUrlParser)
        miniAppStorage.getFileName(TEST_URL)
        verify(localUrlParser, times(1)).getFileName(TEST_URL)
    }

    private fun getMockedMiniAppStorage() = MiniAppStorage (mock(), mock(), mock())

    private fun getMockedLocalUrlParser() = mock<LocalUrlParser>()

}