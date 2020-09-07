package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.sdkutils.AppInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class RealMiniAppSpec {

    private var apiClient: ApiClient = mock()
    private val apiClientRepository: ApiClientRepository = mock()
    private val displayer: Displayer = mock()
    private val miniAppDownloader: MiniAppDownloader = mock()
    private val miniAppInfoFetcher: MiniAppInfoFetcher = mock()
    private val miniAppSdkConfig: MiniAppSdkConfig = mock()
    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    private val realMiniApp =
        RealMiniApp(
            apiClientRepository,
            miniAppDownloader,
            displayer,
            miniAppInfoFetcher,
            miniAppCustomPermissionCache
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

    @Test
    fun `should invoke from MiniAppDownloader and Displayer when calling create miniapp`() =
        runBlockingTest {
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge)

            verify(miniAppDownloader, times(1)).getMiniApp(TEST_MA_ID)
            verify(displayer, times(1))
                .createMiniAppDisplay(getMiniAppResult.first, getMiniAppResult.second,
                    miniAppMessageBridge, null)
        }

    @Test
    fun `should create mini app display with correct passing external navigator`() =
        runBlockingTest {
            val getMiniAppResult = Pair(TEST_BASE_PATH, TEST_MA)
            When calling miniAppDownloader.getMiniApp(TEST_MA_ID) itReturns getMiniAppResult
            realMiniApp.create(TEST_MA_ID, miniAppMessageBridge, miniAppNavigator)

            verify(miniAppDownloader, times(1)).getMiniApp(TEST_MA_ID)
            verify(displayer, times(1))
                .createMiniAppDisplay(getMiniAppResult.first, getMiniAppResult.second,
                    miniAppMessageBridge, miniAppNavigator)
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
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )

        miniApp.updateConfiguration(miniAppSdkConfig)

        verify(miniApp, times(1)).createApiClient(miniAppSdkConfig)
    }

    @Test
    fun `should invoke readPermissions from cache when getCustomPermissions is calling`() {
        val miniAppId = "miniAppId"
        realMiniApp.getCustomPermissions(miniAppId)

        verify(miniAppCustomPermissionCache).readPermissions(miniAppId)
    }

    @Test
    fun `should invoke storePermissions from cache when setCustomPermissions is calling`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            "dummyMiniAppId",
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED)
            )
        )
        realMiniApp.setCustomPermissions(miniAppCustomPermission)

        verify(miniAppCustomPermissionCache).storePermissions(miniAppCustomPermission)
    }
}
