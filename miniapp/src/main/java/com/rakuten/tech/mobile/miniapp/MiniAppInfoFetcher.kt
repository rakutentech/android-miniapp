package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient

internal class MiniAppInfoFetcher(val apiClient: ApiClient) {

    suspend fun fetchMiniAppList() = apiClient.list()

    suspend fun getInfo(appId: String) = apiClient.fetchInfo(appId)
}
