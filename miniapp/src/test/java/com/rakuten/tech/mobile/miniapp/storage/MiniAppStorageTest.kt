package com.rakuten.tech.mobile.miniapp.storage

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MiniAppStorageTest {
    private val miniAppStorage: MiniAppStorage = MiniAppStorage(mock(), mock(), mock())

    @Rule @JvmField
    val tempFolder = TemporaryFolder()

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
    fun `should get consistent path when get path for mini app version`() {
        val storage = MiniAppStorage(FileWriter(), File(TEST_BASE_PATH))

        storage.getMiniAppVersionPath(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBeEqualTo
                "$TEST_BASE_PATH/miniapp/$TEST_ID_MINIAPP/$TEST_ID_MINIAPP_VERSION"
    }

    @Test
    fun `should delete all file data excluding the latest version package`() = runBlockingTest {
        val oldFile1 = tempFolder.newFolder("old_package_id_1")
        val oldFile2 = tempFolder.newFile()
        val latestPackage = tempFolder.newFolder(TEST_ID_MINIAPP_VERSION)

        miniAppStorage.removeOutdatedVersionApp(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION,
            tempFolder.root.path)

        oldFile1.exists() shouldBe false
        oldFile2.exists() shouldBe false
        latestPackage.exists() shouldBe true
    }

    private fun getMockedLocalUrlParser() = mock<UrlToFileInfoParser>()
}
