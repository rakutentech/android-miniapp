package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import android.view.View
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments

/**
 * This class can be use to create miniapp instances.
 * @param context of where the miniapp is loaded.
 * @param config provide the necessary configuration.
 */
class MiniAppView(val context: Context, val config: MiniAppConfig) {

    private var miniAppViewHandler: MiniAppViewHandler =
        config.miniAppSdkConfig?.let { MiniAppViewHandler(context, it) }!!

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a view when successful.
     * @param appId mini app id.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    suspend fun create(miniAppId: String, fromCache: Boolean = false): View? = when {
        miniAppId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppView(miniAppId, config, fromCache)
            .getMiniAppView(context)
    }

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a view when successful.
     * @param appInfo metadata of a mini app.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    suspend fun create(miniAppInfo: MiniAppInfo, fromCache: Boolean = false): View? = when {
        miniAppInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppView(miniAppInfo.id, config, fromCache)
            .getMiniAppView(context)
    }

    /**
     * Creates a mini app using provided url.
     * Mini app is NOT downloaded and cached in local, its content are read directly from the url.
     * This should only be used for previewing a mini app from a local server.
     * @param appUrl a HTTP url containing Mini App content.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    suspend fun createWithUrl(miniAppUrl: String): View? = when {
        miniAppUrl.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppViewWithUrl(miniAppUrl, config).getMiniAppView(context)
    }
}
