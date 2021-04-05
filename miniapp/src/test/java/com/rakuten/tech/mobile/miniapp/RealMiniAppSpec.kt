package com.rakuten.tech.mobile.miniapp

import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.api.*
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.sdkutils.AppInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
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
    val miniAppMessageBridge: MiniAppMessageBridge = mock()
    val miniAppNavigator: MiniAppNavigator = mock()
    val miniAppFileChooser: MiniAppFileChooser = mock()

    @Before
    fun setup() {
        realMiniApp =
            spy(RealMiniApp(apiClientRepository, miniAppDownloader, displayer, miniAppInfoFetcher,
                initCustomPermissionCache = { miniAppCustomPermissionCache },
                initDownloadedManifestCache = { downloadedManifestCache }
            ))

        When calling apiClientRepository.getApiClientFor(miniAppSdkConfig.key) itReturns apiClient
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
        val demoManifest =
            MiniAppManifest(
                listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
                listOf(),
                TEST_ATP_LIST,
                mapOf()
            )
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
        When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
    }

    @Test
    fun `should invoke MiniAppDownloader, Displayer and verifyManifest while miniapp creation`() =
        runBlockingTest {
            onGettingManifestWhileCreate()
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)

            verify(miniAppDownloader).getMiniApp(TEST_MA_ID)
            verify(realMiniApp).verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(displayer).createMiniAppDisplay(
                getMiniAppResult.first, getMiniAppResult.second,
                miniAppMessageBridge, null, null, miniAppCustomPermissionCache, downloadedManifestCache, ""
            )
        }

    @Test
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
                ""
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
                miniAppCustomPermissionCache, downloadedManifestCache, ""
            )
        }

    /** end region */

    /** region: RealMiniApp.updateConfiguration */
    @Test
    fun `should update ApiClient when configuration updated`() {
        realMiniApp.updateConfiguration(miniAppSdkConfig)

        verify(miniAppDownloader).updateApiClient(apiClient)
        verify(miniAppInfoFetcher).updateApiClient(apiClient)
    }

    @Test
    fun `should not create ApiClient for existing configuration`() {
        val miniApp = Mockito.spy(realMiniApp)

        realMiniApp.updateConfiguration(miniAppSdkConfig)

        verify(miniApp, times(0)).createApiClient(miniAppSdkConfig)
    }

    @Test
    fun `should create a new ApiClient when there is no cache`() {
        AppInfo.instance = mock()
        val miniApp = Mockito.spy(realMiniApp)
        val miniAppSdkConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            rasProjectId = TEST_HA_ID_PROJECT,
            isPreviewMode = true,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )

        miniApp.updateConfiguration(miniAppSdkConfig)

        verify(miniApp).createApiClient(miniAppSdkConfig)
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

    /** region: RealMiniApp.listDownloadedWithCustomPermissions */
    @Test
    fun `should invoke getDownloadedMiniAppList from downloader when listDownloadedWithCustomPermissions calls`() {
        realMiniApp.listDownloadedWithCustomPermissions()

        verify(miniAppDownloader).getDownloadedMiniAppList()
    }

    @Test
    fun `should return the correct result when listDownloadedWithCustomPermissions calls`() {
        val miniAppInfo = MiniAppInfo("test_id", "display_name", "test_icon_url",
            Version("test_version_tag", "test_version_id")
        )
        val downloadedList = listOf(miniAppInfo)
        val miniAppCustomPermission = MiniAppCustomPermission("test_id",
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED),
                Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, MiniAppCustomPermissionResult.DENIED))
        )
        doReturn(downloadedList).whenever(miniAppDownloader).getDownloadedMiniAppList()
        downloadedList.forEach {
            doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache).readPermissions(it.id)
        }

        val actual = realMiniApp.listDownloadedWithCustomPermissions()
        val expected = listOf(Pair(miniAppInfo, miniAppCustomPermission))

        assertEquals(expected, actual)
    }
    /** end region */
}

@Suppress("LongMethod")
@ExperimentalCoroutinesApi
class RealMiniAppManifestSpec : BaseRealMiniAppSpec() {
    private val demoManifest = MiniAppManifest(
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")), listOf(), TEST_ATP_LIST, mapOf()
    )
    private val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
    private val deniedPermission = MiniAppCustomPermission(
        TEST_MA_ID,
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
    )

    @Before
    fun before() {
        When calling downloadedManifestCache.readDownloadedManifest(TEST_MA_ID) itReturns cachedManifest
    }

    /** region: RealMiniApp.listDownloadedWithCustomPermissions */
    @Test(expected = RequiredPermissionsNotGrantedException::class)
    fun `verifyManifest will throw exception when required permissions are denied`() =
        runBlockingTest {
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling downloadedManifestCache.isRequiredPermissionDenied(deniedPermission) itReturns true

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
        }

    @Test
    fun `verifyManifest will execute successfully when required permissions are accepted`() =
        runBlockingTest {
            val allowedPermission = MiniAppCustomPermission(
                TEST_MA_ID,
                listOf(
                    Pair(
                        MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED
                    )
                )
            )
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns
                    allowedPermission
            When calling downloadedManifestCache.getAllPermissions(allowedPermission) itReturns
                    allowedPermission.pairValues

            realMiniApp.verifyManifest(TEST_MA_ID, TEST_MA_VERSION_ID)

            verify(downloadedManifestCache, times(0)).storeDownloadedManifest(
                TEST_MA_ID, cachedManifest
            )
            verify(miniAppCustomPermissionCache).removePermissionsNotMatching(
                TEST_MA_ID, allowedPermission.pairValues
            )
        }

    @Test
    fun `verifyManifest will store api manifest when version ids are different`() =
        runBlockingTest {
            val differentVersionId = "another_version_id"
            val manifestToStore = CachedManifest(differentVersionId, demoManifest)
            When calling realMiniApp.getMiniAppManifest(TEST_MA_ID, differentVersionId) itReturns demoManifest
            When calling miniAppCustomPermissionCache.readPermissions(TEST_MA_ID) itReturns deniedPermission
            When calling downloadedManifestCache.getAllPermissions(deniedPermission) itReturns
                    deniedPermission.pairValues

            realMiniApp.verifyManifest(TEST_MA_ID, differentVersionId)

            verify(downloadedManifestCache).storeDownloadedManifest(TEST_MA_ID, manifestToStore)
            verify(miniAppCustomPermissionCache).removePermissionsNotMatching(TEST_MA_ID, deniedPermission.pairValues)
        }

    /** end region */

    @Test
    fun `api manifest should be fetched from MiniAppDownloader`() =
        runBlockingTest {
            realMiniApp.getMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
            verify(miniAppDownloader).fetchMiniAppManifest(TEST_MA_ID, TEST_MA_VERSION_ID)
        }
}
