package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import org.mockito.kotlin.mock
import org.amshove.kluent.shouldBe
import org.junit.Test

class ApiClientRepositorySpec {
    private val apiClientRepository = ApiClientRepository()
    private val miniAppSdkConfig: MiniAppSdkConfig = mock()

    @Test
    fun `should get null when there is no ApiClient stored`() {
        apiClientRepository.getApiClientFor(miniAppSdkConfig) shouldBe null
    }

    @Test
    fun `should get the exact ApiClient which had been stored`() {
        val apiClient: ApiClient = mock()

        apiClientRepository.registerApiClient(miniAppSdkConfig, apiClient)
        apiClientRepository.getApiClientFor(miniAppSdkConfig) shouldBe apiClient
    }
}
