package com.rakuten.tech.mobile.miniapp.api

import android.util.ArrayMap

internal class ApiClientRepository {
    private val apiClients: MutableMap<String, ApiClient> = ArrayMap()

    fun registerApiClient(key: String, apiClient: ApiClient) = apiClients.put(key, apiClient)

    fun getApiClientFor(key: String): ApiClient? = apiClients[key]
}
