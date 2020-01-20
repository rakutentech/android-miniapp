package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.MiniAppLister
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class MiniAppListerSpec {

    @Test
    fun `MiniAppLister should use correct method of api client method for fetching app list`() =
        runBlockingTest {
            val apiClient: ApiClient = mock()
            val miniAppLister = MiniAppLister(apiClient)
            miniAppLister.fetchMiniAppList()
            verify(apiClient, times(1)).list()
        }
}
