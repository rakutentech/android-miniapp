package com.rakuten.tech.mobile.miniapp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class MiniAppInfoFetcherSpec {

    @Test
    fun `When fetching app list then correct method of ApiClient is used`() =
        runBlockingTest {
            val apiClient: ApiClient = mock()
            val miniAppLister = MiniAppInfoFetcher(apiClient)
            miniAppLister.fetchMiniAppList()
            verify(apiClient, times(1)).list()
        }

    @Test
    fun `When fetching metadata then correct method of ApiClient is used`() =
        runBlockingTest {
            val apiClient: ApiClient = mock()
            val miniAppLister = MiniAppInfoFetcher(apiClient)
            miniAppLister.getInfo(TEST_MA_ID)
            verify(apiClient, times(1)).fetchInfo(TEST_MA_ID)
        }
}
