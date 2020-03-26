package com.rakuten.tech.mobile.miniapp

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MiniAppDownloaderSpec {

    private val apiClient: ApiClient = mock()
    private val storage: MiniAppStorage = mock()
    private val miniAppStatus: MiniAppStatus = mock()
    private lateinit var downloader: MiniAppDownloader
    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        downloader = MiniAppDownloader(storage, apiClient, miniAppStatus, dispatcher)
    }

    @Test
    fun `when downloading a mini app then downloader should fetch manifest at first`() {
        runBlocking {
            setupValidManifestResponse(downloader, apiClient)
            downloader.startDownload(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test(expected = MiniAppSdkException::class)
    fun `when downloading a mini app, MiniAppSdkException is thrown in case of invalid manifest`() =
        runBlockingTest {
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
        assertTrue { downloader.isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when Manifest has empty list`() {
        val manifestEntity = ManifestEntity(emptyList())
        assertFalse { downloader.isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when manifest is null`() {
        val manifestEntity = Gson().fromJson("{}", ManifestEntity::class.java)
        assertFalse { downloader.isManifestValid(manifestEntity) }
    }

    @Test
    fun `isManifestValid returns false when files list in manifest is null`() {
        val manifestEntity = Gson().fromJson("""{"files": null}""", ManifestEntity::class.java)
        assertFalse { downloader.isManifestValid(manifestEntity) }
    }

    @Test
    fun `when no existing app in local storage, run download execution`() {
        When calling miniAppStatus.isVersionDownloaded(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION
        ) itReturns false
        When calling storage.getMiniAppPath(TEST_ID_MINIAPP) itReturns TEST_BASE_PATH

        runBlocking {
            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test
    fun `when there is latest existing app in local storage, load the local storage path`() = runBlockingTest {
        When calling miniAppStatus.isVersionDownloaded(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION
        ) itReturns true

        When calling storage.getMiniAppVersionPath(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION
        ) itReturns TEST_BASE_PATH

        setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

        downloader.getMiniApp(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBe TEST_BASE_PATH
    }

    @Test
    fun `should execute old file deletion after downloading new version`() {
        runBlockingTest {
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns false
            When calling storage.getMiniAppPath(TEST_ID_MINIAPP) itReturns TEST_BASE_PATH

            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            verify(storage, times(1)).removeOutdatedVersionApp(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when the version of miniapp is not published`() =
        runBlockingTest {
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
            downloader.getMiniApp(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)
        }

    @Test
    fun `MiniAppDownloader should implement UpdatableApiClient`() {
        downloader shouldBeInstanceOf UpdatableApiClient::class.java
    }

    private suspend fun setupValidManifestResponse(
        downloader: MiniAppDownloader,
        apiClient: ApiClient
    ) {
        When calling downloader.fetchManifest(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION
        ) itReturns ManifestEntity(listOf(TEST_URL_HTTPS_1))

        val mockResponseBody = TEST_BODY_CONTENT.toResponseBody(null)
        When calling apiClient.downloadFile(TEST_URL_HTTPS_1) itReturns mockResponseBody
    }

    private suspend fun setupLatestMiniAppInfoResponse(apiClient: ApiClient, appId: String, versionId: String) {
        When calling apiClient.fetchInfo(appId) itReturns
                MiniAppInfo(id = appId, displayName = TEST_MA_DISPLAY_NAME, icon = "",
                    version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = versionId))
    }
}
