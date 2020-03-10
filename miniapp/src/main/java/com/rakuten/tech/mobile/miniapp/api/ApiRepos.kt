package com.rakuten.tech.mobile.miniapp.api

import android.util.ArrayMap
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig

internal class ApiRepos(private val defaultConfig: MiniAppSdkConfig) {
    private val apiClients: MutableMap<String, ApiClient> = ArrayMap()
    
    fun get(fromConfig: MiniAppSdkConfig?): ApiClient {
        val config = fromConfig ?: defaultConfig

        var apiClient = apiClients[config.key]
        if (apiClient == null) {
            apiClient = createApiClient(config)
            apiClients[config.key] = apiClient
        }

        return apiClient
    }

    private fun createApiClient(config: MiniAppSdkConfig) = ApiClient(
        baseUrl = config.baseUrl,
        rasAppId = config.rasAppId,
        subscriptionKey = config.subscriptionKey,
        hostAppVersionId = config.hostAppVersionId
    )
}
