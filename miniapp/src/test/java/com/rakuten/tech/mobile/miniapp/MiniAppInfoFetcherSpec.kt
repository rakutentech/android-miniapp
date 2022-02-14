package com.rakuten.tech.mobile.miniapp

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
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
            verify(apiClient).list()
        }

    @Test
    fun `When fetching metadata then correct method of ApiClient is used`() =
        runBlockingTest {
            miniAppInfoFetcher.getInfo(TEST_MA_ID)
            verify(apiClient).fetchInfo(TEST_MA_ID)
        }

    @Test
    fun `MiniAppInfoFetcher should implement UpdatableApiClient`() {
        miniAppInfoFetcher shouldBeInstanceOf UpdatableApiClient::class.java
    }

    @Test
    fun `When fetching info by preview code then correct method of ApiClient is used`() =
        runBlockingTest {
            miniAppInfoFetcher.getInfoByPreviewCode("preview-code")
            verify(apiClient).fetchInfoByPreviewCode("preview-code")
        }
}
