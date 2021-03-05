package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.api.*
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.storage.MiniAppManifestCache
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

@Suppress("LargeClass")
@ExperimentalCoroutinesApi
class RealMiniAppSpec {

    private var apiClient: ApiClient = mock()
    private val apiClientRepository: ApiClientRepository = mock()
    private val displayer: Displayer = mock()
    private val miniAppDownloader: MiniAppDownloader = mock()
    private val miniAppInfoFetcher: MiniAppInfoFetcher = mock()
    private val miniAppSdkConfig: MiniAppSdkConfig = mock()
    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    private val manifestCache: MiniAppManifestCache = mock()
    private val realMiniApp =
        RealMiniApp(
            apiClientRepository,
            miniAppDownloader,
            displayer,
            miniAppInfoFetcher,
            initCustomPermissionCache = { miniAppCustomPermissionCache },
            initManifestCache = { manifestCache }
        )
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()
    private val miniAppNavigator: MiniAppNavigator = mock()

    @Before
    fun setup() {
        When calling apiClientRepository.getApiClientFor(miniAppSdkConfig.key) itReturns apiClient
    }

    @Test
    fun `should invoke from MiniAppInfoFetcher when calling list miniapp`() = runBlockingTest {
        realMiniApp.listMiniApp()

        verify(miniAppInfoFetcher, times(1)).fetchMiniAppList()
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when app id is invalid`() = runBlockingTest {
        realMiniApp.fetchInfo("")
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when app id is blank`() = runBlockingTest {
        realMiniApp.create(" ", miniAppMessageBridge)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when id of MiniAppInfo is blank`() = runBlockingTest {
        val testMiniAppInfo = TEST_MA.copy(id = "")
        realMiniApp.create(testMiniAppInfo, miniAppMessageBridge)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should check to call isRequiredPermissionDenied when create miniapp`() = runBlockingTest {
        val demoManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            mapOf()
        )
        realMiniApp.create(" ", miniAppMessageBridge)
        verify(manifestCache).isRequiredPermissionDenied(TEST_MA_ID, demoManifest)
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when required permissions are not granted`() = runBlockingTest {
        val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
        val demoManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            mapOf()
        )
        realMiniApp.temporaryManifest = demoManifest
        val permissionList = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED))
        When calling manifestCache.getCachedRequiredPermissions(TEST_MA_ID) itReturns permissionList
        When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
        When calling manifestCache.isRequiredPermissionDenied(TEST_MA_ID, demoManifest) itReturns true

        realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)
    }

    @Test
    fun `should not throw exception when required permissions are granted`() = runBlockingTest {
        val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
        val demoManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            mapOf()
        )
        realMiniApp.temporaryManifest = demoManifest
        val permissionList = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED))
        When calling manifestCache.getCachedRequiredPermissions(TEST_MA_ID) itReturns permissionList
        When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
        When calling manifestCache.isRequiredPermissionDenied(TEST_MA_ID, demoManifest) itReturns false

        realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)
    }

    @Test
    fun `should invoke from MiniAppDownloader and Displayer when calling create miniapp`() =
        runBlockingTest {
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)

            verify(miniAppDownloader, times(1)).getMiniApp(TEST_MA_ID)
            verify(displayer, times(1))
                .createMiniAppDisplay(
                    getMiniAppResult.first, getMiniAppResult.second,
                    miniAppMessageBridge, null, miniAppCustomPermissionCache, ""
                )
        }

    @Test
    fun `should create mini app display with correct passing external navigator`() =
        runBlockingTest {
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA, miniAppMessageBridge, miniAppNavigator)

            verify(miniAppDownloader, times(1)).getMiniApp(TEST_MA)
            verify(displayer, times(1))
                .createMiniAppDisplay(
                    getMiniAppResult.first,
                    getMiniAppResult.second,
                    miniAppMessageBridge,
                    miniAppNavigator,
                    miniAppCustomPermissionCache,
                    ""
                )
        }

    @Test
    fun `should invoke from MiniAppInfoFetcher when calling get miniapp info`() = runBlockingTest {
        realMiniApp.fetchInfo(TEST_MA_ID)

        verify(miniAppInfoFetcher, times(1))
            .getInfo(TEST_MA_ID)
    }

    @Test
    fun `should update ApiClient when configuration updated`() {
        realMiniApp.updateConfiguration(miniAppSdkConfig)

        verify(miniAppDownloader, times(1)).updateApiClient(apiClient)
        verify(miniAppInfoFetcher, times(1)).updateApiClient(apiClient)
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

        verify(miniApp, times(1)).createApiClient(miniAppSdkConfig)
    }

    @Test
    fun `should invoke getCachedAllPermissions from manifest cache when getCustomPermissions is calling`() {
        val miniAppId = "miniAppId"
        realMiniApp.getCustomPermissions(miniAppId)

        verify(manifestCache).getCachedAllPermissions(miniAppId)
    }

    @Test
    fun `setCustomPermissions should store data found from manifest cache when api is not calling`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )
        When calling manifestCache.getCachedAllPermissions(TEST_MA_ID) itReturns miniAppCustomPermission.pairValues
        realMiniApp.setCustomPermissions(miniAppCustomPermission)

        verify(manifestCache).getCachedAllPermissions(TEST_MA_ID)
        verify(miniAppCustomPermissionCache).storePermissions(miniAppCustomPermission)
    }

    @Test
    fun `setCustomPermissions should store data when api is calling in miniapp instance`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )
        val miniAppManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            mapOf()
        )
        realMiniApp.temporaryManifest = miniAppManifest
        When calling manifestCache.getCachedAllPermissions(TEST_MA_ID) itReturns miniAppCustomPermission.pairValues
        realMiniApp.setCustomPermissions(miniAppCustomPermission)

        verify(manifestCache, times(0)).getCachedAllPermissions(TEST_MA_ID)
        verify(miniAppCustomPermissionCache).storePermissions(miniAppCustomPermission)
    }

    @Test
    @Suppress("LongMethod")
    fun `should invoke getDownloadedMiniAppList from downloader when listDownloadedWithCustomPermissions is calling`() {
        realMiniApp.listDownloadedWithCustomPermissions()

        verify(miniAppDownloader).getDownloadedMiniAppList()
    }

    @Test
    @Suppress("LongMethod")
    fun `should return the correct result when listDownloadedWithCustomPermissions is calling`() {
        val miniAppInfo = MiniAppInfo(
            "test_id",
            "display_name",
            "test_icon_url",
            Version("test_version_tag", "test_version_id")
        )
        val downloadedList = listOf(miniAppInfo)
        val miniAppCustomPermission = MiniAppCustomPermission(
            "test_id",
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.CONTACT_LIST,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )

        doReturn(downloadedList).whenever(miniAppDownloader).getDownloadedMiniAppList()

        downloadedList.forEach {
            doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache)
                .readPermissions(it.id)
        }

        val actual = realMiniApp.listDownloadedWithCustomPermissions()
        val expected = listOf(Pair(miniAppInfo, miniAppCustomPermission))

        assertEquals(expected, actual)
    }

    @Test
    fun `api manifest should be fetched from MiniAppDownloader`() =
        runBlockingTest {
            val miniAppManifest = MiniAppManifest(listOf(), listOf(), emptyMap())
            When calling miniAppDownloader.fetchMiniAppManifest(
                TEST_ID_MINIAPP,
                TEST_ID_MINIAPP_VERSION
            ) itReturns miniAppManifest
            val actual = realMiniApp.getMiniAppManifest(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
            assertEquals(realMiniApp.temporaryManifest, actual)
            verify(miniAppDownloader).fetchMiniAppManifest(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION)
        }

    @Test
    fun `downloaded manifest should be read from manifest cache`() {
        realMiniApp.getDownloadedManifest(TEST_ID_MINIAPP)
        verify(manifestCache).readMiniAppManifest(TEST_ID_MINIAPP)
    }
}
