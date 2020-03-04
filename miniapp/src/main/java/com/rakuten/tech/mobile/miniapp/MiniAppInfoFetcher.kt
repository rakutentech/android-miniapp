package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import java.lang.Exception

internal class MiniAppInfoFetcher(private val apiClient: ApiClient) {

    suspend fun fetchMiniAppList() = apiClient.list()

    @Suppress("TooGenericExceptionCaught")
    suspend fun getInfo(appId: String) = run {
        try {
            apiClient.fetchInfo(appId)
        } catch (error: Exception) {
            // If backend functions correctly, this should never happen
            throw sdkExceptionForInternalServerError()
        }
    }
}
