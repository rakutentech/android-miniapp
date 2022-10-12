package com.rakuten.tech.mobile.miniapp

import android.content.Context
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.DB_NAME_PREFIX
import com.rakuten.tech.mobile.miniapp.js.MessageBridgeRatDispatcher
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.MiniAppSecureStorageDispatcher
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.miniapp.storage.verifier.MiniAppManifestVerifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.assertEquals

open class BaseRealMiniAppSpec {
    internal lateinit var realMiniApp: RealMiniApp
    internal var apiClient: ApiClient = mock()
    private val apiClientRepository: ApiClientRepository = mock()
    internal val displayer: Displayer = mock()
    internal val miniAppDownloader: MiniAppDownloader = mock()
    internal val miniAppInfoFetcher: MiniAppInfoFetcher = mock()
    val miniAppSdkConfig: MiniAppSdkConfig = mock()
    internal val miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    internal val downloadedManifestCache: DownloadedManifestCache = mock()
    internal val manifestVerifier: MiniAppManifestVerifier = mock()
    val miniAppMessageBridge: MiniAppMessageBridge = mock()
    val miniAppNavigator: MiniAppNavigator = mock()
    val miniAppFileChooser: MiniAppFileChooser = mock()

    val dummyManifest: MiniAppManifest = MiniAppManifest(
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")), listOf(),
        TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
    )
    internal var miniAppAnalytics: MiniAppAnalytics = MiniAppAnalytics(
        TEST_HA_ID_PROJECT,
        TEST_HA_ANALYTICS_CONFIGS
    )

    internal var ratDispatcher = MessageBridgeRatDispatcher(miniAppAnalytics)
    internal var secureStorageDispatcher: MiniAppSecureStorageDispatcher = mock()

    @Before
    fun setup() {
        realMiniApp =
            spy(RealMiniApp(apiClientRepository, miniAppDownloader, displayer, miniAppInfoFetcher,
                initCustomPermissionCache = { miniAppCustomPermissionCache },
                initDownloadedManifestCache = { downloadedManifestCache },
                initManifestVerifier = { manifestVerifier },
                miniAppAnalytics = miniAppAnalytics,
                ratDispatcher = ratDispatcher,
                secureStorageDispatcher = secureStorageDispatcher,
                enableH5Ads = false
            ))

        When calling apiClientRepository.getApiClientFor(miniAppSdkConfig) itReturns apiClient
        When calling miniAppSdkConfig.rasProjectId itReturns TEST_HA_ID_PROJECT
        When calling miniAppSdkConfig.miniAppAnalyticsConfigList itReturns TEST_HA_ANALYTICS_CONFIGS
        When calling miniAppSdkConfig.maxStorageSizeLimitInBytes itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES
    }
}

@Suppress("LargeClass")
@ExperimentalCoroutinesApi
class RealMiniAppSpec : BaseRealMiniAppSpec() {

    @Test
    fun `should invoke from MiniAppInfoFetcher when calling list miniapp`() = runBlockingTest {
        realMiniApp.listMiniApp()

        verify(miniAppInfoFetcher).fetchMiniAppList()
    }

    /** region: RealMiniApp.fetchInfo */
    @Test(expected = MiniAppSdkException::class)
    fun `fetchInfo should throw exception when app id is invalid`() = runBlockingTest {
        realMiniApp.fetchInfo("")
    }

    @Test
    fun `should invoke from MiniAppInfoFetcher when calling get miniapp info`() =
        runBlockingTest {
            realMiniApp.fetchInfo(TEST_MA_ID)

            verify(miniAppInfoFetcher).getInfo(TEST_MA_ID)
        }
    /** end region */

    /** region: RealMiniApp.getMiniAppInfoByPreviewCode */
    @Test(expected = MiniAppSdkException::class)
    fun `getMiniAppInfoByPreviewCode should throw exception when preview code is invalid`() = runBlockingTest {
        realMiniApp.getMiniAppInfoByPreviewCode("")
    }

    @Test
    fun `should invoke from MiniAppInfoFetcher when calling get  by preview code`() =
        runBlockingTest {
            realMiniApp.getMiniAppInfoByPreviewCode(TEST_MA_PREVIEW_CODE)

            verify(miniAppInfoFetcher).getInfoByPreviewCode(TEST_MA_PREVIEW_CODE)
        }
    /** end region */

    /** region: RealMiniApp.create */
    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when app id is blank while miniapp creation`() =
        runBlockingTest {
            realMiniApp.create(" ", miniAppMessageBridge)
        }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when id of MiniAppInfo is blank while miniapp creation`() =
        runBlockingTest {
            val testMiniAppInfo = TEST_MA.copy(id = "")
            realMiniApp.create(testMiniAppInfo, miniAppMessageBridge)
        }

    private fun onGettingManifestWhileCreate() = runBlockingTest {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
        When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
        When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID) itReturns dummyManifest
    }

    @Test
    @Suppress("LongMethod")
    fun `should invoke MiniAppDownloader, Displayer and verifyManifest while miniapp creation`() = runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)

            verify(miniAppDownloader).getMiniApp(TEST_MA_ID)
            verify(realMiniApp).verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(displayer).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppMessageBridge,
                null,
                null,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                "",
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                false
            )
        }

    @Test
    @Suppress("LongMethod")
    fun `should invoke getCachedMiniApp, Displayer and verifyCachedManifest while miniapp creation from cache`() =
        runBlockingTest {
            val file: File = mock()
            val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling downloadedManifestCache.getManifestFile(TEST_MA_ID) itReturns file
            When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
            When calling manifestVerifier.verify(TEST_MA_ID, file) itReturns true
            When calling miniAppDownloader.getCachedMiniApp(TEST_MA_ID) itReturns getMiniAppResult

            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge, fromCache = true)

            verify(miniAppDownloader).getCachedMiniApp(TEST_MA_ID)
            verify(realMiniApp).verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
            verify(displayer).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppMessageBridge,
                null,
                null,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                "",
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                false
            )

            When calling miniAppDownloader.getCachedMiniApp(TEST_MA) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA, miniAppMessageBridge, fromCache = true)

            verify(miniAppDownloader).getCachedMiniApp(TEST_MA)
            verify(realMiniApp, times(2)).verifyManifest(
                TEST_MA.id,
                TEST_MA.version.versionId,
                true
            )
            verify(displayer, times(2)).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppMessageBridge,
                null,
                null,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                "",
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                false
            )
        }

    @Test(expected = MiniAppNotFoundException::class)
    fun `MiniAppNotFoundException if manifest can not verify with hash when fromCache true`() = runBlockingTest {
        val file: File = mock()
        When calling downloadedManifestCache.getManifestFile(TEST_MA_ID) itReturns file
        When calling manifestVerifier.verify(TEST_MA_ID, file) itReturns false
        realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
    }

    @Test(expected = MiniAppNotFoundException::class)
    fun `verifyManifest will throw exception if can not find cache manifest when fromCache true`() = runBlockingTest {
        realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
    }

    @Test
    @Suppress("LongMethod")
    fun `should create mini app display with correct passing external navigator`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser)

            verify(miniAppDownloader).getMiniApp(TEST_MA)
            verify(displayer).createMiniAppDisplay(
                getMiniAppResult.first,
                getMiniAppResult.second,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                "",
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                enableH5Ads = false
            )
        }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when url is blank while miniapp creation`() =
        runBlockingTest {
            realMiniApp.createWithUrl(" ", miniAppMessageBridge)
        }

    @Test
    fun `should invoke validateHttpAppUrl while miniapp creation with valid url`() =
        runBlockingTest {
            realMiniApp.createWithUrl(TEST_MA_URL, miniAppMessageBridge)

            verify(miniAppDownloader).validateHttpAppUrl(TEST_MA_URL)
            verify(displayer).createMiniAppDisplay(
                TEST_MA_URL, miniAppMessageBridge, null, null,
                miniAppCustomPermissionCache, downloadedManifestCache, "", miniAppAnalytics,
                ratDispatcher, secureStorageDispatcher, false
            )
        }

    /** end region */

    /** region: RealMiniApp.updateConfiguration */
    @Test
    fun `should update ApiClient when configuration updated`() {
        realMiniApp.updateConfiguration(miniAppSdkConfig, setConfigAsDefault = true)

        verify(miniAppDownloader).updateApiClient(apiClient)
        verify(miniAppInfoFetcher).updateApiClient(apiClient)
    }

    @Test
    fun `should not create ApiClient for existing configuration`() {
        val miniApp = Mockito.spy(realMiniApp)

        realMiniApp.updateConfiguration(miniAppSdkConfig, setConfigAsDefault = true)

        verify(miniApp, times(0)).createApiClient(miniAppSdkConfig)
    }

    @Test
    fun `should create a new ApiClient when there is no cache`() {
        val miniApp = Mockito.spy(realMiniApp)
        val miniAppSdkConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            rasProjectId = TEST_HA_ID_PROJECT,
            isPreviewMode = true,
            requireSignatureVerification = true,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME,
            miniAppAnalyticsConfigList = TEST_HA_ANALYTICS_CONFIGS
        )

        miniApp.updateConfiguration(miniAppSdkConfig, setConfigAsDefault = true)

        verify(miniApp).createApiClient(miniAppSdkConfig)
    }
    /** end region */

    /** region: RealMiniApp.listDownloadedWithCustomPermissions */
    @Test
    fun `should invoke getDownloadedMiniAppList from downloader when listDownloadedWithCustomPermissions calls`() {
        realMiniApp.listDownloadedWithCustomPermissions()

        verify(miniAppDownloader).getDownloadedMiniAppList()
    }

    @Test
    @Suppress("LongMethod")
    fun `should return the correct result when listDownloadedWithCustomPermissions calls`() {
        val miniAppInfo = MiniAppInfo(
            "test_id", "display_name", "test_icon_url",
            Version("test_version_tag", "test_version_id"),
            TEST_PROMOTIONAL_URL, TEST_PROMOTIONAL_TEXT
        )
        val downloadedList = listOf(miniAppInfo)
        val miniAppCustomPermission = MiniAppCustomPermission(
            "test_id",
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
        doReturn(downloadedList).whenever(miniAppDownloader).getDownloadedMiniAppList()
        downloadedList.forEach {
            doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache).readPermissions(
                it.id
            )
        }

        val actual = realMiniApp.listDownloadedWithCustomPermissions()
        val expected = listOf(Pair(miniAppInfo, miniAppCustomPermission))

        assertEquals(expected, actual)
    }
    /** end region */

    /** region: custom permissions setter / getter */
    @Test
    fun `getCustomPermissions should get data from custom permission cache`() {
        realMiniApp.getCustomPermissions(TEST_MA_ID)

        verify(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)
    }

    @Test
    fun `setCustomPermissions should store data in custom permission cache`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )
        realMiniApp.setCustomPermissions(miniAppCustomPermission)

        verify(miniAppCustomPermissionCache).storePermissions(miniAppCustomPermission)
    }
    /** end region */

    /** region: secure storage */
    private val context: Context = mock()
    private val databaseList = arrayOf(DB_NAME_PREFIX + TEST_MA_ID, "sample_database")

    @Test
    fun `clearSecureStorages should clear all the mini appp secure storages`() {
        When calling context.databaseList() itReturns databaseList
        realMiniApp.clearSecureStorages(context = context)
        verify(context).deleteDatabase(DB_NAME_PREFIX + TEST_MA_ID)
    }

    @Test
    fun `clearSecureStorages should not delete database except miniapp sceure storage`() {
        When calling context.databaseList() itReturns databaseList

        realMiniApp.clearSecureStorages(context = context)

        verify(context, times(1)).deleteDatabase(DB_NAME_PREFIX + TEST_MA_ID)
        verify(context, times(0)).deleteDatabase("sample_database")
    }

    @Test
    fun `clearSecureStorages should not throw any exception if can not clear secure storages`() {
        When calling context.databaseList() itReturns null
        realMiniApp.clearSecureStorages(context = context)
    }

    @Test
    fun `clearSecureStorage should call deleteDatabase with specific mini app secure storages`() {
        realMiniApp.clearSecureStorage(context, TEST_MA_ID)
        verify(context).deleteDatabase(DB_NAME_PREFIX + TEST_MA_ID)
    }

    @Test
    fun `clearSecureStorage should return true if the specific mini app secure storages is cleared`() {
        When calling context.databaseList() itReturns arrayOf("sample_database")
        realMiniApp.clearSecureStorage(context, TEST_MA_ID) shouldBe true
    }

    @Test
    fun `clearSecureStorage should return false if the specific mini app secure storages can not be cleared`() {
        When calling context.databaseList() itReturns databaseList
        realMiniApp.clearSecureStorage(context, TEST_MA_ID) shouldBe false
    }

    @Test
    fun `clearSecureStorage should return false if any exception happened`() {
        When calling context.databaseList() itReturns null
        realMiniApp.clearSecureStorage(context, TEST_MA_ID) shouldBe false
    }
    /** end region */
}

@Suppress("LongMethod")
@ExperimentalCoroutinesApi
class RealMiniAppManifestSpec : BaseRealMiniAppSpec() {
    private val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
    private val deniedPermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
    )
    private val file: File = mock()

    @Before
    fun before() {
        When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
        When calling downloadedManifestCache.getManifestFile(TEST_MA_ID) itReturns file
        When calling manifestVerifier.verify(TEST_MA_ID, file) itReturns true
    }

    /** region: Manifest verification */
    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied`() =
        runBlockingTest {
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID) itReturns dummyManifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling downloadedManifestCache.isRequiredPermissionDenied(deniedPermission) itReturns true

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
        }

    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifydManifest will throw exception when required permissions are denied when fromCache true`() =
        runBlockingTest {
            When calling realMiniApp.getMiniAppManifest(
                TEST_MA_ID,
                TEST_MA_VERSION_ID
            ) itReturns dummyManifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling downloadedManifestCache.isRequiredPermissionDenied(deniedPermission) itReturns true

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID, true)
        }

    @Test
    fun `verifyManifest will execute successfully when required permissions are accepted`() =
        runBlockingTest {
            val allowedPermission = MiniAppCustomPermission(
                TEST_MA_ID,
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED))
            )

            val manifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns manifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns allowedPermission
            When calling downloadedManifestCache.getAllPermissions(allowedPermission) itReturns
                    allowedPermission.pairValues
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID) itReturns dummyManifest

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(downloadedManifestCache, times(0)).storeDownloadedManifest(
                TEST_MA_ID, cachedManifest
            )
            verify(realMiniApp).isManifestEqual(dummyManifest, dummyManifest)
            verify(miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID, allowedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will store api manifest when version ids are different`() =
        runBlockingTest {
            val differentVersionId = "another_version_id"
            val manifestToStore = CachedManifest(differentVersionId, dummyManifest)
            val manifest = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, differentVersionId) itReturns dummyManifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling downloadedManifestCache.getAllPermissions(deniedPermission) itReturns
                    deniedPermission.pairValues

            realMiniApp.verifyManifest(TEST_MA_ID, differentVersionId)

            verify(realMiniApp).checkToDownloadManifest(TEST_MA_ID, differentVersionId, manifest)
            verify(downloadedManifestCache).storeDownloadedManifest(TEST_MA_ID, manifestToStore)
            verify(miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID,
                deniedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will download api manifest when hash has not been verified`() =
        runBlockingTest {
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID) itReturns dummyManifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling manifestVerifier.verify(TEST_MA_ID, file) itReturns false

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(realMiniApp, times(2)).checkToDownloadManifest(TEST_MA_ID, TEST_MA_VERSION_ID, cachedManifest)
        }

    @Test
    fun `checkToDownloadManifest will store manifest and hash properly`() =
        runBlockingTest {
            val manifest = MiniAppManifest(
                    listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason"),
                            Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason")), listOf(),
                    TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
            )
            val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, manifest)
            val manifestToStore = CachedManifest(TEST_MA_VERSION_ID, dummyManifest)
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID) itReturns dummyManifest
            realMiniApp.checkToDownloadManifest(TEST_MA_ID, TEST_MA_VERSION_ID, cachedManifest)

            verify(downloadedManifestCache).storeDownloadedManifest(TEST_MA_ID, manifestToStore)
            verify(manifestVerifier).storeHashAsync(TEST_MA_ID, file)
        }

    @Test
    fun `isManifestEqual will return true when both api and downloaded manifest are equal`() {
        realMiniApp.isManifestEqual(dummyManifest, dummyManifest) shouldBeEqualTo true
    }

    @Test
    fun `isManifestEqual will return false when api and downloaded manifest are not equal`() {
        val dummyApiManifest = MiniAppManifest(
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason"),
                        Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason")),
                listOf(),
                TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        realMiniApp.isManifestEqual(dummyApiManifest, dummyManifest) shouldBeEqualTo false
    }

    @Test
    fun `isManifestEqual will return false when api and downloaded manifest are null`() {
        realMiniApp.isManifestEqual(null, null) shouldBeEqualTo false
    }
    /** end region */

    @Test
    fun `api manifest should be fetched from MiniAppDownloader`() =
        runBlockingTest {
            realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(miniAppDownloader).fetchMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, "")
        }

    @Test
    fun `api manifest should not be fetched from MiniAppDownloader when different languageCode`() =
        runBlockingTest {
            realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE)
            verify(miniAppDownloader, times(0))
                .fetchMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID, "")
        }

    @Test
    fun `getDownloadedManifest should read data from cache`() {
        realMiniApp.getDownloadedManifest(TEST_MA_ID)
        verify(downloadedManifestCache).readDownloadedManifest(TEST_MA_ID)
    }
}
