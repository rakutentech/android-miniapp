package com.rakuten.tech.mobile.miniapp.signatureverifier.api

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.signatureverifier.PublicKeyFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PublicKeyFetcherSpec {

    companion object {
        private const val KEY_ID = "test_id"
    }

    private val apiClient: ApiClient = mock()
    private val publicKeyFetcher = PublicKeyFetcher(apiClient)

    @Test
    fun `When fetching public key then correct method of ApiClient is used`() =
        runBlockingTest {
            publicKeyFetcher.fetch(KEY_ID)
            verify(apiClient).fetchPublicKey(KEY_ID)
        }
}
