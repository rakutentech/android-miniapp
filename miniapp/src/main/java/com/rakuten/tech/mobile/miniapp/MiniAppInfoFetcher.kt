package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient

internal class MiniAppInfoFetcher(private var apiClient: ApiClient) : UpdatableApiClient {

    @Throws(MiniAppSdkException::class)
    suspend fun fetchMiniAppList() = apiClient.list()

    @Throws(MiniAppSdkException::class)
    suspend fun getInfo(appId: String) = apiClient.fetchInfo(appId)

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }
}
