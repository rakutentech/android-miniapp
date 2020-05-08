package com.rakuten.tech.mobile.miniapp.api

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.junit.Test

class ApiClientRepositorySpec {
    private val apiClientRepository = ApiClientRepository()

    @Test
    fun `should get null when there is no ApiClient stored`() {
        apiClientRepository.getApiClientFor("key") shouldBe null
    }

    @Test
    fun `should get the exact ApiClient which had been stored`() {
        val apiClient: ApiClient = mock()

        apiClientRepository.registerApiClient("key", apiClient)
        apiClientRepository.getApiClientFor("key") shouldBe apiClient
    }
}
