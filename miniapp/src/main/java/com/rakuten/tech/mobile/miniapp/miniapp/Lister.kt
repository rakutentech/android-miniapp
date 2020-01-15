package com.rakuten.tech.mobile.miniapp.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient

internal class Lister(val apiClient: ApiClient) {

    suspend fun fetchMiniAppList() = apiClient.list()
}
