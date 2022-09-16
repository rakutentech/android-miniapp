package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppInfo

/**
 * This class can be used in the HostApp to create the mini app views using mini app id.
 */
sealed class MiniAppParameters {
    /**
     * This class can be used in the HostApp to create the mini app views using mini app id.
     * @param context is used by the view for initializing the internal services.
     * Must be the context of activity to ensure that all standard html components work properly.
     * @param config provide the necessary configuration to provide an independent MiniApp.
     * @param miniAppId mini app id.
     * @param fromCache allow host app to load miniapp from cache.
     */
    data class DefaultParams(
        val context: Context,
        val config: MiniAppConfig,
        val miniAppId: String,
        val miniAppVersion: String,
        val fromCache: Boolean = false
    ) : MiniAppParameters()

    /**
     * This class can be used in the HostApp to create the mini app views using mini app id.
     * @param context is used by the view for initializing the internal services.
     * Must be the context of activity to ensure that all standard html components work properly.
     * @param config provide the necessary configuration to provide an independent MiniApp.
     * @param miniAppInfo metadata of a mini app.
     * @param fromCache allow host app to load miniapp from cache.
     */
    data class InfoParams(
        val context: Context,
        val config: MiniAppConfig,
        val miniAppInfo: MiniAppInfo,
        val fromCache: Boolean = false
    ) : MiniAppParameters()

    /**
     * This class can be used in the HostApp to create the mini app views using provided url.
     * @param context is used by the view for initializing the internal services.
     * Must be the context of activity to ensure that all standard html components work properly.
     * @param config provide the necessary configuration to provide an independent MiniApp.
     * Mini app is NOT downloaded and cached in local, its content are read directly from the url.
     * This should only be used for previewing a mini app from a local server.
     * @param miniAppUrl a HTTP url containing Mini App content.
     */
    data class UrlParams(
        val context: Context,
        val config: MiniAppConfig,
        val miniAppUrl: String
    ) : MiniAppParameters()
}
