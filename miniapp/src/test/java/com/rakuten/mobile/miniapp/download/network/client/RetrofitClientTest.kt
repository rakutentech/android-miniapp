package com.rakuten.mobile.miniapp.download.network.client

import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.download.DownloadBaseTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Test class for RetrofitClient.
 */
@RunWith(JUnit4::class)
class RetrofitClientTest : DownloadBaseTest() {

    private lateinit var retrofitClient: RetrofitClient

    @Before
    fun setup() {
        retrofitClient = RetrofitClient()
    }

    @Test
    fun shouldSetBaseURLInRetrofit() {
        assertThat(retrofitClient.retrofit.baseUrl().url().toString()).isEqualTo(BASE_URL)
    }
}
