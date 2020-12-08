package com.rakuten.tech.mobile.miniapp

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.storage.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.*
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("LargeClass")
@ExperimentalCoroutinesApi
class MiniAppDownloaderSpec {

    private val apiClient: ApiClient = mock()
    private val storage: MiniAppStorage = mock()
    private val miniAppStatus: MiniAppStatus = mock()
    private val verifier: CachedMiniAppVerifier = mock()
    private lateinit var downloader: MiniAppDownloader
    private val dispatcher = TestCoroutineDispatcher()
    private val testMiniApp = TEST_MA.copy(
        id = TEST_ID_MINIAPP,
        version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = TEST_ID_MINIAPP_VERSION)
    )

    @Before
    fun setup() {
        downloader = MiniAppDownloader(storage, mock(), miniAppStatus, verifier, dispatcher)
        downloader.updateApiClient(apiClient)

        When calling verifier.verify(any(), any()) itReturns true
    }

    @Test
    fun `when downloading a mini app then downloader should fetch manifest at first`() {
        runBlocking {
            setupValidManifestResponse(downloader, apiClient)
            downloader.startDownload(testMiniApp)

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
            downloader.startDownload(testMiniApp)
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
        When calling storage.getMiniAppVersionPath(
            TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) itReturns TEST_BASE_PATH
        When calling storage.getMiniAppPath(TEST_ID_MINIAPP) itReturns TEST_BASE_PATH

        runBlocking {
            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP)

            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test
    fun `when there is latest existing app in local storage, load the local storage path`() =
        runBlockingTest {
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION,
                TEST_BASE_PATH
            ) itReturns true
            When calling storage.getMiniAppVersionPath(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns TEST_BASE_PATH

            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP).first shouldBe TEST_BASE_PATH
        }

    @Test
    fun `should execute old file deletion after downloading new version when in host mode`() = runBlockingTest {
        When calling storage.getMiniAppVersionPath(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION) itReturns TEST_BASE_PATH
        When calling miniAppStatus.isVersionDownloaded(
            TEST_ID_MINIAPP,
            TEST_ID_MINIAPP_VERSION,
            TEST_BASE_PATH) itReturns false

        setupValidManifestResponse(downloader, apiClient)
        setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

        downloader.getMiniApp(TEST_ID_MINIAPP)

        When calling apiClient.isPreviewMode itReturns true
        downloader.getMiniApp(TEST_ID_MINIAPP)

        verify(storage, times(1)).removeVersions(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
    }

    @Test
    fun `should download old version when it is no longer in storage and being published`() {
        runBlockingTest {
            When calling storage.getMiniAppVersionPath(
                TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) itReturns TEST_BASE_PATH
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION, TEST_BASE_PATH) itReturns false

            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP)

            verify(storage, times(1)).removeVersions(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test
    fun `when cached mini app verification fails, it should re-download the mini app`() {
        When calling verifier.verify(any(), any()) itReturns false
        When calling storage.getMiniAppVersionPath(
            TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION
        ) itReturns TEST_BASE_PATH
        When calling miniAppStatus.isVersionDownloaded(
            TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION, TEST_BASE_PATH
        ) itReturns true

        runBlocking {
            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP)

            verify(apiClient, times(1)).fetchFileList(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            )
        }
    }

    @Test
    fun `when cached mini app verification fails, it should delete all downloaded versions for the mini app id`() {
        runBlockingTest {
            When calling verifier.verify(any(), any()) itReturns false
            When calling storage.getMiniAppVersionPath(
                TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION
            ) itReturns TEST_BASE_PATH
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION, TEST_BASE_PATH
            ) itReturns true

            setupValidManifestResponse(downloader, apiClient)
            setupLatestMiniAppInfoResponse(apiClient, TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)

            downloader.getMiniApp(TEST_ID_MINIAPP)

            Verify on storage that storage.removeApp(
                eq(TEST_ID_MINIAPP),
                anyOrNull()
            ) was called
        }
    }

    @Test
    fun `MiniAppDownloader should implement UpdatableApiClient`() {
        downloader shouldBeInstanceOf UpdatableApiClient::class.java
    }

    @Test
    fun `when there is network issue, load the local storage path if existed`() =
        runBlockingTest {
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION,
                TEST_BASE_PATH
            ) itReturns true
            When calling miniAppStatus.getDownloadedMiniApp(TEST_ID_MINIAPP) itReturns testMiniApp
            When calling storage.getMiniAppVersionPath(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns TEST_BASE_PATH
            When calling apiClient.fetchInfo(TEST_ID_MINIAPP) doThrow MiniAppNetException(TEST_ERROR_MSG)

            downloader.getMiniApp(TEST_ID_MINIAPP).first shouldBe TEST_BASE_PATH
            downloader.getMiniApp(testMiniApp).first shouldBe TEST_BASE_PATH
        }

    @Test(expected = MiniAppSdkException::class)
    fun `when there is network issue and cached mini app verification fails, it should throw an exception`() =
        runBlockingTest {
            When calling verifier.verify(any(), any()) itReturns false
            When calling miniAppStatus.isVersionDownloaded(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION,
                TEST_BASE_PATH
            ) itReturns true
            When calling miniAppStatus.getDownloadedMiniApp(TEST_ID_MINIAPP) itReturns testMiniApp
            When calling storage.getMiniAppVersionPath(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns TEST_BASE_PATH
            When calling apiClient.fetchInfo(TEST_ID_MINIAPP) doThrow MiniAppNetException(TEST_ERROR_MSG)

            downloader.getMiniApp(TEST_ID_MINIAPP).first shouldBe TEST_BASE_PATH
        }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when cannot get miniapp by id from server`() = runBlockingTest {
        When calling apiClient.fetchInfo(TEST_ID_MINIAPP) doThrow MiniAppNetException(TEST_ERROR_MSG)

        downloader.getMiniApp(TEST_ID_MINIAPP)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when cannot get miniapp by MiniAppInfo from server`() = runBlockingTest {
        When calling apiClient.fetchFileList(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) doThrow
                MiniAppNetException(TEST_ERROR_MSG)

        downloader.getMiniApp(testMiniApp)
    }

    @Test
    fun `getDownloadedMiniAppList should get values from miniAppStatus`() {
        val actual = downloader.getDownloadedMiniAppList()
        val expected = miniAppStatus.getDownloadedMiniAppList()

        assertEquals(expected, actual)
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

    private suspend fun setupLatestMiniAppInfoResponse(
        apiClient: ApiClient,
        appId: String,
        versionId: String
    ) {
        When calling apiClient.fetchInfo(appId) itReturns
                MiniAppInfo(
                    id = appId, displayName = TEST_MA_DISPLAY_NAME, icon = "",
                    version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = versionId)
                )
    }
}
