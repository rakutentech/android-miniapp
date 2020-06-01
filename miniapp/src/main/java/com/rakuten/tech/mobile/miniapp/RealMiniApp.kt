package com.rakuten.tech.mobile.miniapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge

internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override suspend fun create(
        context: Context,
        info: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = executingCreate(context, info, miniAppMessageBridge)

    @Suppress("TooGenericExceptionThrown")
    override suspend fun create(info: MiniAppInfo): MiniAppDisplay =
        executingCreate(null, info, object : MiniAppMessageBridge() {
            override fun getUniqueId(): String = throw Exception("MiniAppMessageBridge has not been implemented")
        })

    private suspend fun executingCreate(
        context: Context?,
        info: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = when {
        info.id.isBlank() || info.version.versionId.isBlank() ->
            throw sdkExceptionForInvalidArguments()
        else -> {
            val basePath = miniAppDownloader.getMiniApp(
                appId = info.id,
                versionId = info.version.versionId
            )
            displayer.createMiniAppDisplay(context, basePath, info.id, miniAppMessageBridge)
        }
    }

    override fun updateConfiguration(newConfig: MiniAppSdkConfig) {
        var nextApiClient = apiClientRepository.getApiClientFor(newConfig.key)
        if (nextApiClient == null) {
            nextApiClient = createApiClient(newConfig)
            apiClientRepository.registerApiClient(newConfig.key, nextApiClient)
        }

        nextApiClient.also {
            miniAppDownloader.updateApiClient(it)
            miniAppInfoFetcher.updateApiClient(it)
        }
    }

    @VisibleForTesting
    internal fun createApiClient(newConfig: MiniAppSdkConfig) =
        ApiClient(
            baseUrl = newConfig.baseUrl,
            rasAppId = newConfig.rasAppId,
            subscriptionKey = newConfig.subscriptionKey,
            hostAppVersionId = newConfig.hostAppVersionId
        )
}
