package com.rakuten.tech.mobile.miniapp.api

import androidx.collection.ArrayMap
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig

internal class ApiClientRepository {
    private val apiClients: MutableMap<MiniAppSdkConfig, ApiClient> = ArrayMap()

    fun registerApiClient(config: MiniAppSdkConfig, apiClient: ApiClient) = apiClients.put(config, apiClient)

    fun getApiClientFor(config: MiniAppSdkConfig): ApiClient? = apiClients[config]
}
