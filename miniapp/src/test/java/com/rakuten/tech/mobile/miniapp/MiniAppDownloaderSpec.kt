package com.rakuten.tech.mobile.miniapp

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniAppDownloaderSpec {

    @Test
    fun `when downloading a mini app then downloader should fetch manifest at first`() =
        runBlockingTest {
            val apiClient: ApiClient = mock()
            val storage: MiniAppStorage = mock()
            val downloader = MiniAppDownloader(storage, apiClient)

            When calling downloader.fetchManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns ManifestEntity(listOf(TEST_URL_HTTPS_1))

            val mockResponseBody = TEST_BODY_CONTENT.toResponseBody(null)
            When calling apiClient.downloadFile(TEST_URL_HTTPS_1) itReturns mockResponseBody
            downloader.startDownload(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }

    @Test(expected = MiniAppSdkException::class)
    fun `when downloading a mini app, MiniAppSdkException is thrown in case of invalid manifest`() =
        runBlockingTest {
            val downloader = getMockedDownloader()
            When calling downloader.fetchManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns ManifestEntity(emptyList())
            downloader.startDownload(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }

    @Test
    fun `isManifestValid returns true for valid Manifest`() {
        val manifestEntity = ManifestEntity(files = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2))
        assertTrue { getMockedDownloader().isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when Manifest has empty list`() {
        val manifestEntity = ManifestEntity(emptyList())
        assertFalse { getMockedDownloader().isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when manifest is null`() {
        val manifestEntity = Gson().fromJson("{}", ManifestEntity::class.java)
        assertFalse { getMockedDownloader().isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when files list in manifest is null`() {
        val manifestEntity = Gson().fromJson("""{"files": null}""", ManifestEntity::class.java)
        assertFalse { getMockedDownloader().isManifestValid(manifestEntity) }
    }

    private fun getMockedDownloader(): MiniAppDownloader {
        val apiClient: ApiClient = mock()
        val storage: MiniAppStorage = mock()
        val downloader = MiniAppDownloader(storage, apiClient)
        return downloader
    }
}
