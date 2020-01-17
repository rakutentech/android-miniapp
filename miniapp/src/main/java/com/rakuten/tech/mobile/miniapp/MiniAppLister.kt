package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient

internal class MiniAppLister(val apiClient: ApiClient) {

    suspend fun fetchMiniAppList() = apiClient.list()
}
