package com.rakuten.tech.mobile.miniapp

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.storage.verifier.CachedMiniAppVerifier
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

@Suppress("LargeClass")
open class MiniAppDownloaderBaseSpec {
    internal val apiClient: ApiClient = mock()
    private val miniAppAnalytics: MiniAppAnalytics = mock()
    internal val storage: MiniAppStorage = mock()
    internal val miniAppStatus: MiniAppStatus = mock()
    internal val verifier: CachedMiniAppVerifier = mock()
    internal val manifestApiCache: ManifestApiCache = mock()
    private val signatureVerifier: SignatureVerifier = mock()
    internal lateinit var downloader: MiniAppDownloader
    private val dispatcher = TestCoroutineDispatcher()
    internal val testMiniApp = TEST_MA.copy(
        id = TEST_ID_MINIAPP,
        version = Version(versionTag = TEST_MA_VERSION_TAG, versionId = TEST_ID_MINIAPP_VERSION)
    )
    internal val dummyManifest = MiniAppManifest(
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "")),
        listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "")),
        TEST_ATP_LIST, emptyMap(), TEST_MA_VERSION_ID
    )
    internal val requiredPermissionObj =
        MetadataPermissionObj("rakuten.miniapp.user.USER_NAME", "reason")
    internal val optionalPermissionObj =
        MetadataPermissionObj("rakuten.miniapp.user.PROFILE_PHOTO", "reason")

    @Before
    fun setup() {
        downloader = spy(
            MiniAppDownloader(
                apiClient = apiClient,
                miniAppAnalytics = miniAppAnalytics,
                isRequireSignatureVerification = false,
                initStorage = { storage },
                initStatus = { miniAppStatus },
                initVerifier = { verifier },
                initManifestApiCache = { manifestApiCache },
                initSignatureVerifier = { signatureVerifier },
                coroutineDispatcher = dispatcher
            )
        )
        downloader.updateApiClient(apiClient)

        When calling verifier.verify(any(), any()) itReturns true
    }

    internal suspend fun setupLatestMiniAppInfoResponse(
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
@ExperimentalCoroutinesApi
@SuppressWarnings("LargeClass")
class MiniAppDownloaderSpec : MiniAppDownloaderBaseSpec() {

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
            ) itReturns Pair(
                ManifestEntity(emptyList(), TEST_PUBLIC_KEY_ID),
                ManifestHeader(TEST_MANIFEST_SIGNATURE)
            )
            downloader.startDownload(testMiniApp)
        }

    @Test
    fun `isManifestFileExist returns true for valid Manifest`() {
        val manifestEntity = ManifestEntity(
            files = listOf(TEST_URL_HTTPS_1, TEST_URL_HTTPS_2),
            publicKeyId = TEST_PUBLIC_KEY_ID
        )
        assertTrue { downloader.isManifestFileExist(manifestEntity) }
    }

    @Test
    fun `isManifestFileExist returns false when Manifest has empty list`() {
        val manifestEntity = ManifestEntity(emptyList(), TEST_PUBLIC_KEY_ID)
        assertFalse { downloader.isManifestFileExist(manifestEntity) }
    }

    @Test
    fun `isManifestFileExist returns false when manifest is null`() {
        val manifestEntity = Gson().fromJson("{}", ManifestEntity::class.java)
        assertFalse { downloader.isManifestFileExist(manifestEntity) }
    }

    @Test
    fun `isManifestFileExist returns false when files list in manifest is null`() {
        val manifestEntity = Gson().fromJson("""{"files": null}""", ManifestEntity::class.java)
        assertFalse { downloader.isManifestFileExist(manifestEntity) }
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
    fun `when there is latest existing app in local storage but in preview mode, run download execution`() =
        runBlockingTest {
            When calling apiClient.isPreviewMode itReturns true
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

            downloader.getMiniApp(TEST_ID_MINIAPP)

            verify(apiClient).fetchFileList(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
        }

    @Test
    fun `when there is latest existing app in local storage, load the local storage path`() = runBlockingTest {
        When calling apiClient.isPreviewMode itReturns false
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
    fun `metadata manifest should be fetched from api and be stored when cache returns null`() = runBlockingTest {
        val metadataEntity = MetadataEntity(
            MetadataResponse(
                listOf(requiredPermissionObj), listOf(optionalPermissionObj), TEST_ATP_LIST, hashMapOf()
            )
        )
        When calling manifestApiCache.readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns null
        When calling downloader.prepareMiniAppManifest(metadataEntity, TEST_MA_VERSION_ID) itReturns dummyManifest
        When calling apiClient.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns metadataEntity

        val actual = downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)

        assertEquals(dummyManifest, actual)
        verify(manifestApiCache, times(1)).storeManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID, dummyManifest)
    }

    @Test
    fun `should not store manifest when in preview mode`() = runBlockingTest {
        val metadataEntity = MetadataEntity(
            MetadataResponse(
                listOf(requiredPermissionObj), listOf(optionalPermissionObj), TEST_ATP_LIST, hashMapOf()
            )
        )
        When calling apiClient.isPreviewMode itReturns true
        When calling manifestApiCache.readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns null
        When calling downloader.prepareMiniAppManifest(metadataEntity, TEST_MA_VERSION_ID) itReturns dummyManifest
        When calling apiClient.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns metadataEntity

        downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)

        verify(manifestApiCache, times(0)).storeManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID, dummyManifest)
    }

    @Test
    fun `metadata manifest should not be fetched from api when cache returns manifest`() = runBlockingTest {
        When calling manifestApiCache.readManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID) itReturns dummyManifest
        val actual = downloader.fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_MA_VERSION_ID)

        assertEquals(dummyManifest, actual)
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

    @SuppressWarnings("LongMethod")
    @Test
    @Suppress("LongMethod")
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

            val actual = downloader.prepareMiniAppManifest(metadataEntity, TEST_MA_VERSION_ID)
            val requiredPermissions =
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason for user name"))
            val optionalPermissions =
                listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason for profile photo"))
            val expected = MiniAppManifest(
                requiredPermissions, optionalPermissions,
                TEST_ATP_LIST, hashMapOf(), TEST_MA_VERSION_ID
            )

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

            val actual = downloader.prepareMiniAppManifest(metadataEntity, "")
            val expected = MiniAppManifest(emptyList(), emptyList(), emptyList(), emptyMap(), "")

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
        ) itReturns Pair(
            ManifestEntity(listOf(TEST_URL_HTTPS_1), TEST_PUBLIC_KEY_ID),
            ManifestHeader(TEST_MANIFEST_SIGNATURE)
        )

        val mockResponseBody = TEST_BODY_CONTENT.toResponseBody(null)
        When calling apiClient.downloadFile(TEST_URL_HTTPS_1) itReturns mockResponseBody
    }
}
