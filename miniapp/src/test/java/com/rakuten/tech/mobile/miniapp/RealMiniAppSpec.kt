package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RealMiniAppSpec {
    private val apiClientRepository: ApiClientRepository = mock()
    private val miniAppDownloader: MiniAppDownloader = mock()
    private val displayer: Displayer = mock()
    private val miniAppInfoFetcher: MiniAppInfoFetcher = mock()
    private val realMiniApp = RealMiniApp(apiClientRepository, miniAppDownloader, displayer, miniAppInfoFetcher)
    private val miniAppSdkConfig: MiniAppSdkConfig = mock()
    private var apiClient: ApiClient = mock()
    private val miniAppMessageInterface: MiniAppMessageInterface = mock()

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
    fun `should throw exception when version id is invalid`() = runBlockingTest {
        realMiniApp.create(TEST_MA_ID, "", miniAppMessageInterface)
    }

    @Test
    fun `should invoke from MiniAppDownloader and Displayer when calling create miniapp`() = runBlockingTest {
        realMiniApp.create(TEST_MA_ID, TEST_MA_VERSION_ID, miniAppMessageInterface)

        val basePath: String = verify(miniAppDownloader, times(1))
            .getMiniApp(TEST_MA_ID, TEST_MA_VERSION_ID)
        verify(displayer, times(1)).createMiniAppDisplay(basePath, TEST_MA_ID, miniAppMessageInterface)
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
    fun `should not cache ApiClient for existing configuration`() {
        realMiniApp.updateConfiguration(miniAppSdkConfig)

        verify(apiClientRepository, times(0))
            .registerApiClient(miniAppSdkConfig.key, apiClient)
    }
}
