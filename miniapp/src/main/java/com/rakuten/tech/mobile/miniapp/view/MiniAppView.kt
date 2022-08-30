package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import android.view.View
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.display.RealMiniAppDisplay
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments

/**
 * This class can be used in the HostApp to create the miniapp views independently.
 * @param context is used by the view for initializing the internal services.
 * Must be the context of activity to ensure that all standard html components work properly.
 * @param config provide the necessary configuration to provide an independent MiniApp.
 */
class MiniAppView(val context: Context, val config: MiniAppConfig) {

    private var miniAppViewHandler: MiniAppViewHandler =
        MiniAppViewHandler(context, config.miniAppSdkConfig)

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a view when successful.
     * @param miniAppId mini app id.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class,
        RequiredPermissionsNotGrantedException::class
    )
    suspend fun load(miniAppId: String, fromCache: Boolean = false): MiniAppDisplay = when {
        miniAppId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppView(miniAppId, config, fromCache)
    }

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a view when successful.
     * @param miniAppInfo metadata of a mini app.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class,
        RequiredPermissionsNotGrantedException::class
    )
    suspend fun load(miniAppInfo: MiniAppInfo, fromCache: Boolean = false): MiniAppDisplay = when {
        miniAppInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppView(miniAppInfo.id, config, fromCache)
    }

    /**
     * Creates a mini app using provided url.
     * Mini app is NOT downloaded and cached in local, its content are read directly from the url.
     * This should only be used for previewing a mini app from a local server.
     * @param miniAppUrl a HTTP url containing Mini App content.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    @Throws(MiniAppNotFoundException::class, MiniAppSdkException::class)
    suspend fun loadWithUrl(miniAppUrl: String): MiniAppDisplay = when {
        miniAppUrl.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppViewHandler.createMiniAppViewWithUrl(miniAppUrl, config)
    }
}
