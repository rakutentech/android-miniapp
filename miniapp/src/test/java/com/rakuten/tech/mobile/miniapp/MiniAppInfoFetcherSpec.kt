package com.rakuten.tech.mobile.miniapp

import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test

class MiniAppInfoFetcherSpec {

    private val apiClient: ApiClient = mock()
    private val miniAppInfoFetcher = MiniAppInfoFetcher(mock())

    @Before
    fun setup() {
        miniAppInfoFetcher.updateApiClient(apiClient)
    }

    @Test
    fun `When fetching app list then correct method of ApiClient is used`() =
        runBlockingTest {
            miniAppInfoFetcher.fetchMiniAppList()
            verify(apiClient, times(1)).list()
        }

    @Test
    fun `When fetching metadata then correct method of ApiClient is used`() =
        runBlockingTest {
            miniAppInfoFetcher.getInfo(TEST_MA_ID)
            verify(apiClient, times(1)).fetchInfo(TEST_MA_ID)
        }

    @Test
    fun `MiniAppInfoFetcher should implement UpdatableApiClient`() {
        miniAppInfoFetcher shouldBeInstanceOf UpdatableApiClient::class.java
    }
}
