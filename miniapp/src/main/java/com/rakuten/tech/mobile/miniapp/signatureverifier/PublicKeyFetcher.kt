package com.rakuten.tech.mobile.miniapp.signatureverifier

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.api.ApiClient

internal class PublicKeyFetcher(private var apiClient: ApiClient) {

    @Throws(MiniAppSdkException::class)
    suspend fun fetch(keyId: String) = apiClient.fetchPublicKey(keyId)
}
