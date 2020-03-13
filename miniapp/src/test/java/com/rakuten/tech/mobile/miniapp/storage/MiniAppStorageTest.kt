package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MiniAppStorageTest {
    private val context: Context = getApplicationContext()
    private val miniAppStorage: MiniAppStorage = MiniAppStorage(mock(), mock(), mock())

    @Test
    fun `for a given set of app & version id formed base path is returned`() {
        assertTrue {
            miniAppStorage.getSavePathForApp(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) == "null/miniapp/$TEST_ID_MINIAPP/$TEST_ID_MINIAPP_VERSION"
        }
    }

    @Test
    fun `for a given set of base path & file path, formed parent path is returned`() {
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

    @Test
    fun `should delete all file data excluding the latest version package`() = runBlocking {
        val directoryPath = context.filesDir.path
        val parentFile = File(directoryPath)
        When calling miniAppStorage.getParentPathApp(TEST_ID_MINIAPP) itReturns directoryPath
        When calling File(miniAppStorage.getParentPathApp(TEST_ID_MINIAPP)) itReturns parentFile

        val files = arrayOf(
            File("$directoryPath/$TEST_ID_MINIAPP"),
            File("$directoryPath/old_package_id")
        )

        When calling miniAppStorage.getChildFiles(parentFile) itReturns files

        miniAppStorage.removeOutdatedVersionApp(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
    }

    private fun getMockedLocalUrlParser() = mock<UrlToFileInfoParser>()
}
