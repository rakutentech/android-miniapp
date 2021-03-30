package com.rakuten.tech.mobile.miniapp

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.api.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.amshove.kluent.*
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("LargeClass", "LongMethod")
@ExperimentalCoroutinesApi
class MiniAppDownloaderSpec {
    private val apiClient: ApiClient = mock()
    private val storage: MiniAppStorage = mock()
    private val miniAppStatus: MiniAppStatus = mock()
    private val verifier: CachedMiniAppVerifier = mock()
    private val manifestApiCache: ManifestApiCache = mock()
    private lateinit var downloader: MiniAppDownloader
    private val dispatcher = TestCoroutineDispatcher()
    private val testMiniApp = TEST_MA.copy(
        id = TEST_ID_MINIAPP,
        version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = TEST_ID_MINIAPP_VERSION)
    )
    private val requiredPermissionObj =
        MetadataPermissionObj("rakuten.miniapp.user.USER_NAME", "reason")
    private val optionalPermissionObj =
        MetadataPermissionObj("rakuten.miniapp.user.PROFILE_PHOTO", "reason")

    @Before
    fun setup() {
        downloader = spy(
            MiniAppDownloader(
                apiClient = apiClient,
                initStorage = { storage },
                initStatus = { miniAppStatus },
                initVerifier = { verifier },
                initManifestApiCache = { manifestApiCache },
                coroutineDispatcher = dispatcher
            )
        )
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
    fun `metadata manifest should be fetched from api when cache returns null`() =
        runBlockingTest {
            val metadataEntity = MetadataEntity(
                MetadataResponse(
                    listOf(requiredPermissionObj), listOf(optionalPermissionObj), TEST_ATP_LIST, hashMapOf()
                )
            )
            val apiManifest = MiniAppManifest(
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "")),
                listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "")),
                TEST_ATP_LIST,
                emptyMap()
            )

            When calling manifestApiCache.readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns null
            When calling downloader.prepareMiniAppManifest(metadataEntity) itReturns apiManifest
            When calling apiClient.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns metadataEntity

            val actual = downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)

            assertEquals(apiManifest, actual)
            verify(manifestApiCache).readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)
            verify(manifestApiCache).storeManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID, apiManifest)
        }

    @Test
    fun `metadata manifest should not be fetched from api when cache returns manifest`() =
        runBlockingTest {
            val metadataEntity = MetadataEntity(
                MetadataResponse(
                    listOf(requiredPermissionObj),
                    listOf(optionalPermissionObj),
                    TEST_ATP_LIST,
                    hashMapOf()
                )
            )
            val cachedManifest = MiniAppManifest(
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "")),
                listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "")),
                TEST_ATP_LIST,
                emptyMap()
            )

            When calling manifestApiCache.readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns cachedManifest
            val actual = downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)

            assertEquals(cachedManifest, actual)
            verify(manifestApiCache).readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)
            verify(downloader, times(0)).prepareMiniAppManifest(metadataEntity)
            verify(apiClient, times(0)).fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)
            verify(manifestApiCache, times(0)).storeManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID, cachedManifest)
        }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when cannot get metadata for invalid version id`() =
        runBlockingTest {
            val metadataEntity = MetadataEntity(
                MetadataResponse(
                    listOf(requiredPermissionObj),
                    listOf(optionalPermissionObj),
                    TEST_ATP_LIST,
                    hashMapOf()
                )
            )

            When calling apiClient.fetchMiniAppManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns metadataEntity

            downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, "")
        }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when cannot get metadata from server`() = runBlockingTest {
        When calling apiClient.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) doThrow
                MiniAppSdkException(TEST_ERROR_MSG)

        downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
    }

    @Test
    fun `prepareMiniAppManifest should return values correctly`() =
        runBlockingTest {
            val metadataEntity = MetadataEntity(
                MetadataResponse(
                    listOf(requiredPermissionObj), listOf(optionalPermissionObj),
                    TEST_ATP_LIST, hashMapOf()
                )
            )
            val requiredPermission =
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason for user name"))
            val optionalPermission =
                listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason for profile photo"))

            When calling downloader.listOfPermissions(listOf(requiredPermissionObj)) itReturns requiredPermission
            When calling downloader.listOfPermissions(listOf(optionalPermissionObj)) itReturns optionalPermission

            val actual = downloader.prepareMiniAppManifest(metadataEntity)
            val requiredPermissions =
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason for user name"))
            val optionalPermissions =
                listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason for profile photo"))
            val expected = MiniAppManifest(requiredPermissions, optionalPermissions, TEST_ATP_LIST, hashMapOf())

            assertEquals(expected, actual)
        }

    @Test
    fun `prepareMiniAppManifest should return empty values correctly`() =
        runBlockingTest {
            val metadataEntity = MetadataEntity(MetadataResponse(
                null, null, emptyList(), null))

            When calling apiClient.fetchMiniAppManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns metadataEntity

            val actual = downloader.prepareMiniAppManifest(metadataEntity)
            val expected = MiniAppManifest(emptyList(), emptyList(), emptyList(), emptyMap())

            assertEquals(expected, actual)
        }

    @Test
    fun `listOfPermissions should return values correctly`() =
        runBlockingTest {
            val actual = downloader.listOfPermissions(listOf(requiredPermissionObj))
            val expected = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason"))

            assertEquals(expected, actual)
        }

    @Test
    fun `listOfPermissions should return empty when there is any unknown permission`() =
        runBlockingTest {
            val unknownPermissionObj =
                MetadataPermissionObj("", "reason")
            val actual = downloader.listOfPermissions(listOf(unknownPermissionObj))
            val expected = ArrayList<Pair<MiniAppCustomPermissionType, String>>()

            assertEquals(expected, actual)
        }

    @Test
    fun `getDownloadedMiniAppList should get values from miniAppStatus`() {
        val actual = downloader.getDownloadedMiniAppList()
        val expected = miniAppStatus.getDownloadedMiniAppList()

        assertEquals(expected, actual)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw error when download with invalid url`() {
        downloader.validateHttpAppUrl("invalid_url")
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw error when cannot connect to server`() {
        downloader.validateHttpAppUrl(TEST_URL_HTTPS_1)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when there is internal server error`() {
        When calling downloader.validateHttpAppUrl(TEST_URL_HTTPS_1) itThrows IOException(
            TEST_ERROR_MSG)
        downloader.validateHttpAppUrl(TEST_URL_HTTPS_1)
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
