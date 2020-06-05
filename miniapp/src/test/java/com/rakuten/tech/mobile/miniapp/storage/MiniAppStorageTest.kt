package com.rakuten.tech.mobile.miniapp.storage

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import com.rakuten.tech.mobile.miniapp.TEST_URL_FILE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MiniAppStorageTest {
    private val fileWriter: FileWriter = mock()
    private val miniAppStorage: MiniAppStorage = MiniAppStorage(fileWriter, mock(), mock())
    private val zipFile = "test.zip"

    @Rule @JvmField
    val tempFolder = TemporaryFolder()

    @After
    fun onFinish() {
        tempFolder.delete()
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

    @Test
    fun `should extract file with FileWriter`() = runBlockingTest {
        val file = tempFolder.newFile()
        When calling miniAppStorage.getFilePath(file.path) itReturns file.path
        When calling miniAppStorage.getFileName(file.path) itReturns file.name
        val inputStream: InputStream = mock()
        miniAppStorage.saveFile(file.path, file.path, inputStream)

        verify(fileWriter, times(1))
            .unzip(inputStream, miniAppStorage.getAbsoluteWritePath(
                file.path, miniAppStorage.getFilePath(file.path), miniAppStorage.getFileName(file.path)))
    }

    @Test
    fun `should unzip file without exception`() = runBlockingTest {
        val file = tempFolder.newFile()
        val filePath = file.path
        val folder = tempFolder.newFolder()
        val folderPath = folder.path
        val containerPath = file.parent

        val fileWriter = FileWriter(TestCoroutineDispatcher())
        zipFiles(containerPath, arrayOf(filePath, folderPath))
        val inputStream = File("$containerPath/$zipFile").inputStream()

        fileWriter.unzip(inputStream, containerPath)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when file path is invalid`() = runBlockingTest {
        val file = File("")
        miniAppStorage.saveFile(file.path, file.path, mock())
    }

    private fun getMockedLocalUrlParser() = mock<UrlToFileInfoParser>()

    @Suppress("NestedBlockDepth", "LongMethod")
    private fun zipFiles(outputPath: String, filePaths: Array<String>) {
        ZipOutputStream(BufferedOutputStream(
            FileOutputStream("$outputPath/$zipFile"))).use { out ->
            for (filePath in filePaths) {
                val file = File(filePath)
                if (file.isDirectory) {
                    out.putNextEntry(ZipEntry(file.name + "/"))
                } else {
                    FileInputStream(filePath).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(filePath.substring(filePath.lastIndexOf("/")))
                            out.putNextEntry(entry)
                            origin.copyTo(out, 1024)
                        }
                    }
                }
            }
        }
    }
}
